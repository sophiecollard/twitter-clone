package twitterclone.api.v1.tweet

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import io.circe.generic.semiauto
import io.circe.Encoder
import org.http4s.server.AuthMiddleware
import org.http4s.{Header, Headers, Method, Request, Status, Uri}
import org.scalatest.EitherValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.typelevel.ci.CIString
import twitterclone.api.authentication.dummyAuthMiddleware
import twitterclone.api.model.PostTweetRequest
import twitterclone.api.v1.tweet.TweetApiEndpoints
import twitterclone.fixtures.tweet._
import twitterclone.instances.ioTransactor
import twitterclone.model.user.User
import twitterclone.model.{Id, Tweet}
import twitterclone.repositories.interpreters.local.LocalTweetRepository
import twitterclone.services.tweet.TweetService
import twitterclone.services.tweet.auth.byAuthor
import twitterclone.testsyntax.{CirceEntityDecoderOps, CirceEntityEncoderOps}

import scala.collection.concurrent.TrieMap

class TweetApiEndpointsSpec extends AnyWordSpec with EitherValues with Matchers {
  "The POST /tweets endpoint" when {
    "the user is authenticated" should {
      "create and return a new tweet" in new Fixtures {
        private val repoState = TrieMap.empty[Id[Tweet], Tweet]
        private val endpoints = newEndpoints(repoState)
        private val userId = Id.random[User]
        private val requestBody = PostTweetRequest(
          "Mieux vaut mobiliser son intelligence sur des betises que mobiliser sa betise sur des choses intelligentes."
        )

        private val request = Request[IO](
          method = Method.POST,
          uri = Uri.unsafeFromString("/"),
          body = requestBody.asEntityBody,
          headers = Headers(Header.Raw(CIString("x-user-id"), userId.value.toString))
        )

        private val response = endpoints
          .httpRoutes
          .orNotFound
          .run(request)
          .unsafeRunSync()

        response.status shouldBe Status.Created

        private val tweet = response.decodeBodyAs[Tweet].unsafeRunSync().value

        tweet.authorId shouldBe userId
        tweet.contents shouldBe requestBody.contents
        repoState.get(tweet.id) shouldBe Some(tweet)
      }
    }

    "the user is not authenticated" should {
      "respond with an unauthorized error" in new Fixtures {
        private val repoState = TrieMap.empty[Id[Tweet], Tweet]
        private val endpoints = newEndpoints(repoState)
        private val requestBody = PostTweetRequest(
          "Mieux vaut mobiliser son intelligence sur des betises que mobiliser sa betise sur des choses intelligentes."
        )

        private val request = Request[IO](
          method = Method.POST,
          uri = Uri.unsafeFromString("/"),
          body = requestBody.asEntityBody
        )

        private val response = endpoints
          .httpRoutes
          .orNotFound
          .run(request)
          .unsafeRunSync()

        response.status shouldBe Status.Unauthorized
      }
    }
  }

  "The DELETE /tweets/{tweet_id} endpoint" when {
    "the user is authenticated and is the author of the tweet" should {
      "delete the tweet" in new Fixtures {
        private val repoState = TrieMap.from((tweet.id, tweet) :: Nil)
        private val endpoints = newEndpoints(repoState)

        private val request = Request[IO](
          method = Method.DELETE,
          uri = Uri.unsafeFromString(s"/${tweet.id.value.toString}"),
          headers = Headers(Header.Raw(CIString("x-user-id"), tweet.authorId.value.toString))
        )

        private val response = endpoints
          .httpRoutes
          .orNotFound
          .run(request)
          .unsafeRunSync()

        response.status shouldBe Status.Ok
        repoState.get(tweet.id) shouldBe None
      }
    }

    "the user is authenticated but is not the author of the tweet" should {
      "respond with an unauthorized error" in new Fixtures {
        private val repoState = TrieMap.from((tweet.id, tweet) :: Nil)
        private val endpoints = newEndpoints(repoState)
        private val userId = Id.random[User]

        private val request = Request[IO](
          method = Method.DELETE,
          uri = Uri.unsafeFromString(s"/${tweet.id.value.toString}"),
          headers = Headers(Header.Raw(CIString("x-user-id"), userId.value.toString))
        )

        private val response = endpoints
          .httpRoutes
          .orNotFound
          .run(request)
          .unsafeRunSync()

        response.status shouldBe Status.Unauthorized
        repoState.get(tweet.id) shouldBe Some(tweet)
      }
    }

    "the user is not authenticated" should {
      "respond with an unauthorized error" in new Fixtures {
        private val repoState = TrieMap.from((tweet.id, tweet) :: Nil)
        private val endpoints = newEndpoints(repoState)

        private val request = Request[IO](
          method = Method.DELETE,
          uri = Uri.unsafeFromString(s"/${tweet.id.value.toString}")
        )

        private val response = endpoints
          .httpRoutes
          .orNotFound
          .run(request)
          .unsafeRunSync()

        response.status shouldBe Status.Unauthorized
        repoState.get(tweet.id) shouldBe Some(tweet)
      }
    }
  }

  "The GET /tweets/{tweet_id} endpoint" should {
    "return the tweet with the specified id" in new Fixtures {
      private val repoState = TrieMap.from((tweet.id, tweet) :: Nil)
      private val endpoints = newEndpoints(repoState)

      private val request = Request[IO](
        method = Method.GET,
        uri = Uri.unsafeFromString(s"/${tweet.id.value.toString}")
      )

      private val response = endpoints
        .httpRoutes
        .orNotFound
        .run(request)
        .unsafeRunSync()

      response.status shouldBe Status.Ok
      response.decodeBodyAs[Tweet].unsafeRunSync().value shouldBe tweet
    }
  }

  "The GET /tweets?author={user_id} endpoint" should {
    "return a list of tweets from the specified author" in new Fixtures {
      private val repoState = TrieMap.from(
        (tweet.id, tweet) ::
          (earlierTweetFromSameAuthor.id, earlierTweetFromSameAuthor) ::
          (tweetFromAnotherAuthor.id, tweetFromAnotherAuthor) :: Nil)
      private val endpoints = newEndpoints(repoState)

      private val request = Request[IO](
        method = Method.GET,
        uri = Uri.unsafeFromString(s"/?author=${tweet.authorId.value.toString}")
      )

      private val response = endpoints
        .httpRoutes
        .orNotFound
        .run(request)
        .unsafeRunSync()

      response.status shouldBe Status.Ok
      response
        .decodeBodyAs[List[Tweet]]
        .unsafeRunSync()
        .value shouldBe List(tweet, earlierTweetFromSameAuthor)
    }
  }

  "The GET /tweets endpoint" should {
    "return a list of tweets" in new Fixtures {
      private val repoState = TrieMap.from(
        (tweet.id, tweet) ::
          (earlierTweetFromSameAuthor.id, earlierTweetFromSameAuthor) ::
          (tweetFromAnotherAuthor.id, tweetFromAnotherAuthor) :: Nil)
      private val endpoints = newEndpoints(repoState)

      private val request = Request[IO](
        method = Method.GET,
        uri = Uri.unsafeFromString("/")
      )

      private val response = endpoints
        .httpRoutes
        .orNotFound
        .run(request)
        .unsafeRunSync()

      response.status shouldBe Status.Ok
      response
        .decodeBodyAs[List[Tweet]]
        .unsafeRunSync()
        .value shouldBe List(tweet, earlierTweetFromSameAuthor, tweetFromAnotherAuthor)
    }
  }
}

trait Fixtures {

  implicit val runtime: IORuntime = cats.effect.unsafe.IORuntime.global

  val authMiddleware: AuthMiddleware[IO, Id[User]] = dummyAuthMiddleware

  def newEndpoints(repoState: TrieMap[Id[Tweet], Tweet]): TweetApiEndpoints[IO] = {
    val tweetRepository = LocalTweetRepository.create[IO](repoState)
    val authByAuthorService = byAuthor(tweetRepository)
    val tweetService = TweetService.create[IO, IO](tweetRepository, authByAuthorService)
    TweetApiEndpoints(dummyAuthMiddleware[IO], tweetService)
  }

  implicit val newTweetRequestBodyEncoder: Encoder[PostTweetRequest] =
    semiauto.deriveEncoder

}

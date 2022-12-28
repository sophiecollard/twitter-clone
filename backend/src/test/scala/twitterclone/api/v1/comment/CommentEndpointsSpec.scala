package twitterclone.api.v1.comment

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import io.circe.Encoder
import io.circe.generic.semiauto
import org.http4s.{Header, Headers, Method, Request, Status, Uri}
import org.http4s.server.AuthMiddleware
import org.scalatest.EitherValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.typelevel.ci.CIString
import twitterclone.api.authentication.dummyAuthMiddleware
import twitterclone.api.v1.comment.{CommentEndpoints, PostCommentRequest}
import twitterclone.fixtures.comment._
import twitterclone.instances.ioTransactor
import twitterclone.model.user.User
import twitterclone.model.{Comment, Id, Tweet}
import twitterclone.repositories.interpreters.local.LocalCommentRepository
import twitterclone.services.comment.CommentService
import twitterclone.services.comment.auth.byAuthor
import twitterclone.testsyntax.{CirceEntityDecoderOps, CirceEntityEncoderOps}

import scala.collection.concurrent.TrieMap

class CommentEndpointsSpec extends AnyWordSpec with EitherValues with Matchers {
  "The POST /comments endpoint" when {
    "the user is authenticated" should {
      "create and return a new comment" in new Fixtures {
        private val repoState = TrieMap.empty[Id[Comment], Comment]
        private val endpoints = newEndpoints(repoState)
        private val userId = Id.random[User]
        private val tweetId = Id.random[Tweet]
        private val requestBody = PostCommentRequest(
          tweetId = tweetId,
          contents = "Mieux vaut mobiliser son intelligence sur des betises que mobiliser sa betise sur des choses intelligentes."
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

        private val comment = response.decodeBodyAs[Comment].unsafeRunSync().value

        comment.authorId shouldBe userId
        comment.tweetId shouldBe tweetId
        comment.contents shouldBe requestBody.contents
        repoState.get(comment.id) shouldBe Some(comment)
      }
    }

    "the user is not authenticated" should {
      "respond with an unauthorized error" in new Fixtures {
        private val repoState = TrieMap.empty[Id[Comment], Comment]
        private val endpoints = newEndpoints(repoState)
        private val tweetId = Id.random[Tweet]
        private val requestBody = PostCommentRequest(
          tweetId = tweetId,
          contents = "Mieux vaut mobiliser son intelligence sur des betises que mobiliser sa betise sur des choses intelligentes."
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

  "The DELETE /comments/{comment_id} endpoint" when {
    "the user is authenticated and is the author of the comment" should {
      "delete the comment" in new Fixtures {
        private val repoState = TrieMap.from((comment.id, comment) :: Nil)
        private val endpoints = newEndpoints(repoState)

        private val request = Request[IO](
          method = Method.DELETE,
          uri = Uri.unsafeFromString(s"/${comment.id.value.toString}"),
          headers = Headers(Header.Raw(CIString("x-user-id"), comment.authorId.value.toString))
        )

        private val response = endpoints
          .httpRoutes
          .orNotFound
          .run(request)
          .unsafeRunSync()

        response.status shouldBe Status.Ok
        repoState.get(comment.id) shouldBe None
      }
    }

    "the user is authenticated but is not the author of the comment" should {
      "respond with an unauthorized error" in new Fixtures {
        private val repoState = TrieMap.from((comment.id, comment) :: Nil)
        private val endpoints = newEndpoints(repoState)
        private val userId = Id.random[User]

        private val request = Request[IO](
          method = Method.DELETE,
          uri = Uri.unsafeFromString(s"/${comment.id.value.toString}"),
          headers = Headers(Header.Raw(CIString("x-user-id"), userId.value.toString))
        )

        private val response = endpoints
          .httpRoutes
          .orNotFound
          .run(request)
          .unsafeRunSync()

        response.status shouldBe Status.Unauthorized
        repoState.get(comment.id) shouldBe Some(comment)
      }
    }

    "the user is not authenticated" should {
      "respond with an unauthorized error" in new Fixtures {
        private val repoState = TrieMap.from((comment.id, comment) :: Nil)
        private val endpoints = newEndpoints(repoState)

        private val request = Request[IO](
          method = Method.DELETE,
          uri = Uri.unsafeFromString(s"/${comment.id.value.toString}")
        )

        private val response = endpoints
          .httpRoutes
          .orNotFound
          .run(request)
          .unsafeRunSync()

        response.status shouldBe Status.Unauthorized
        repoState.get(comment.id) shouldBe Some(comment)
      }
    }
  }

  "The GET /comments/{comment_id} endpoint" should {
    "return the comment with the specified id" in new Fixtures {
      private val repoState = TrieMap.from((comment.id, comment) :: Nil)
      private val endpoints = newEndpoints(repoState)

      private val request = Request[IO](
        method = Method.GET,
        uri = Uri.unsafeFromString(s"/${comment.id.value.toString}")
      )

      private val response = endpoints
        .httpRoutes
        .orNotFound
        .run(request)
        .unsafeRunSync()

      response.status shouldBe Status.Ok
      response.decodeBodyAs[Comment].unsafeRunSync().value shouldBe comment
    }
  }

  "The GET /comments?tweet-id={tweet_id}" should {
    "return a list of comments for the specified tweet" in new Fixtures {
      private val repoState = TrieMap.from(
        (comment.id, comment) ::
          (earlierCommentOnSameTweet.id, earlierCommentOnSameTweet) ::
          (commentOnAnotherTweet.id, commentOnAnotherTweet) :: Nil)
      private val endpoints = newEndpoints(repoState)

      private val request = Request[IO](
        method = Method.GET,
        uri = Uri.unsafeFromString(s"/?tweet-id=${comment.tweetId.value.toString}")
      )

      private val response = endpoints
        .httpRoutes
        .orNotFound
        .run(request)
        .unsafeRunSync()

      response.status shouldBe Status.Ok
      response
        .decodeBodyAs[List[Comment]]
        .unsafeRunSync()
        .value shouldBe List(comment, earlierCommentOnSameTweet)
    }
  }
}

trait Fixtures {

  implicit val runtime: IORuntime = cats.effect.unsafe.IORuntime.global

  val authMiddleware: AuthMiddleware[IO, Id[User]] = dummyAuthMiddleware

  def newEndpoints(repoState: TrieMap[Id[Comment], Comment]): CommentEndpoints[IO] = {
    val commentRepository = LocalCommentRepository.create[IO](repoState)
    val authByAuthorService = byAuthor(commentRepository)
    val commentService = CommentService.create[IO, IO](commentRepository, authByAuthorService)
    CommentEndpoints.create(dummyAuthMiddleware[IO], commentService)
  }

  implicit val newCommentRequestBodyEncoder: Encoder[PostCommentRequest] =
    semiauto.deriveEncoder

}

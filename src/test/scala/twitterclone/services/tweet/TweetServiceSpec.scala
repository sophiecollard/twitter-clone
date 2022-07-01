package twitterclone.services.tweet

import cats.{Id => CatsId}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import twitterclone.auth.error.AuthorizationError.NotTheTweetsAuthor
import twitterclone.model.{Id, Tweet, TweetPagination, User}
import twitterclone.repositories.tweet.LocalTweetRepository
import twitterclone.services.error.ServiceError.ResourceNotFound
import twitterclone.services.tweet.auth.byAuthor
import twitterclone.testinstances._
import twitterclone.testsyntax._

import java.time.{LocalDate, LocalDateTime, LocalTime}
import scala.collection.concurrent.TrieMap

class TweetServiceSpec extends AnyWordSpec with Matchers {
  "The create method" should {
    "create and return a new tweet" in new Fixtures {
      private val repoState = TrieMap.empty[Id[Tweet], Tweet]
      private val service = newService(repoState)
      private val userId = Id.random[User]
      private val contents: String =
        "Mieux vaut mobiliser son intelligence sur des betises que mobiliser sa betise sur des choses intelligentes."

      withNoServiceError(service.create(contents)(userId)) { tweet =>
        tweet.author shouldBe userId
        tweet.contents shouldBe contents
        repoState.get(tweet.id) shouldBe Some(tweet)
      }
    }
  }

  "The delete method" when {
    "the user making the request is the tweet's author" should {
      "delete the tweet" in new Fixtures {
        private val repoState = TrieMap.from((tweet.id, tweet) :: Nil)
        private val service = newService(repoState)

        withSuccessfulAuthorization(service.delete(tweet.id)(tweet.author)) {
          withNoServiceError(_) { _ =>
            repoState.get(tweet.id) shouldBe None
          }
        }
      }
    }

    "the user making the request is not the tweet's author" should {
      "return an error" in new Fixtures {
        private val repoState = TrieMap.from((tweet.id, tweet) :: Nil)
        private val service = newService(repoState)
        private val randomUserId = Id.random[User]

        withFailedAuthorization(service.delete(tweet.id)(randomUserId)) { error =>
          error shouldBe NotTheTweetsAuthor(randomUserId, tweet.id)
          repoState.get(tweet.id) shouldBe Some(tweet)
        }
      }
    }

    "the specified tweet id doesn't exist" should {
      "return an error" in new Fixtures {
        private val repoState = TrieMap.from((tweet.id, tweet) :: Nil)
        private val service = newService(repoState)
        private val userId = Id.random[User]
        private val randomTweetId = Id.random[Tweet]

        withFailedAuthorization(service.delete(randomTweetId)(userId)) { error =>
          error shouldBe NotTheTweetsAuthor(userId, randomTweetId)
        }
      }
    }
  }

  "The get method" when {
    "the specified tweet id exists" should {
      "get the tweet" in new Fixtures {
        private val repoState = TrieMap.from((tweet.id, tweet) :: Nil)
        private val service = newService(repoState)

        withNoServiceError(service.get(tweet.id)) { returnedTweet =>
          returnedTweet shouldBe tweet
        }
      }
    }

    "the specified tweet id doesn't exist" should {
      "return an error" in new Fixtures {
        private val repoState = TrieMap.from((tweet.id, tweet) :: Nil)
        private val service = newService(repoState)
        private val randomTweetId = Id.random[Tweet]

        withServiceError(service.get(randomTweetId)) { error =>
          error shouldBe ResourceNotFound(randomTweetId, "Tweet")
        }
      }
    }
  }

  "The list method" when {
    "given an author id" should {
      "return a list of tweets from this author" in new Fixtures {
        private val repoState = TrieMap.from(
          (tweet.id, tweet) ::
            (earlierTweetFromSameAuthor.id, earlierTweetFromSameAuthor) ::
            (tweetFromAnotherAuthor.id, tweetFromAnotherAuthor) :: Nil)
        private val service = newService(repoState)

        withNoServiceError(service.list(tweet.author)) { tweets =>
          tweets.size shouldBe 2
          tweets should contain theSameElementsAs List(tweet, earlierTweetFromSameAuthor)
          tweets should not contain tweetFromAnotherAuthor
        }
      }

      "only return as many tweets as specified in the pagination" in new Fixtures {
        private val repoState = TrieMap.from(
          (tweet.id, tweet) ::
            (earlierTweetFromSameAuthor.id, earlierTweetFromSameAuthor) ::
            (tweetFromAnotherAuthor.id, tweetFromAnotherAuthor) :: Nil)
        private val service = newService(repoState)
        private val pagination = TweetPagination(pageSize = 1, postedBefore = None)

        withNoServiceError(service.list(tweet.author, pagination)) { tweets =>
          tweets.size shouldBe 1
          tweets should contain (tweet)
        }
      }

      "only return tweets posted before the date specified in the pagination" in new Fixtures {
        private val repoState = TrieMap.from(
          (tweet.id, tweet) ::
            (earlierTweetFromSameAuthor.id, earlierTweetFromSameAuthor) ::
            (tweetFromAnotherAuthor.id, tweetFromAnotherAuthor) :: Nil)
        private val service = newService(repoState)
        private val pagination = TweetPagination(pageSize = 10, postedBefore = Some(tweet.postedOn))

        withNoServiceError(service.list(tweet.author, pagination)) { tweets =>
          tweets.size shouldBe 1
          tweets should contain (earlierTweetFromSameAuthor)
          tweets should not contain tweet
          tweets should not contain tweetFromAnotherAuthor
        }
      }
    }

    "no tweets exist from the specified author" should {
      "return an empty list" in new Fixtures {
        private val repoState = TrieMap.from(
          (tweet.id, tweet) ::
            (earlierTweetFromSameAuthor.id, earlierTweetFromSameAuthor) ::
            (tweetFromAnotherAuthor.id, tweetFromAnotherAuthor) :: Nil)
        private val service = newService(repoState)
        private val randomUserId = Id.random[User]

        withNoServiceError(service.list(randomUserId)) { tweets =>
          tweets shouldBe empty
        }
      }
    }
  }
}

trait Fixtures {

  val tweet: Tweet = Tweet(
    id = Id.random[Tweet],
    author = Id.random[User],
    contents =
      "Mieux vaut mobiliser son intelligence sur des betises que mobiliser sa betise sur des choses intelligentes.",
    postedOn = LocalDateTime.of(
      LocalDate.of(1968, 4, 30),
      LocalTime.of(19, 30)
    )
  )

  val earlierTweetFromSameAuthor: Tweet = Tweet(
    id = Id.random[Tweet],
    author = tweet.author,
    contents = "Je dis des choses tellement intelligentes que souvent, je ne comprends pas ce que je dis.",
    postedOn = LocalDateTime.of(
      LocalDate.of(1968, 4, 29),
      LocalTime.of(19, 30)
    )
  )

  val tweetFromAnotherAuthor: Tweet = Tweet(
    id = Id.random[Tweet],
    author = Id.random[User],
    contents = "S'il n'a a pas de solution, c'est qu'il n'y a pas de problème.",
    postedOn = LocalDateTime.of(
      LocalDate.of(1968, 1, 1),
      LocalTime.of(19, 30)
    )
  )

  def newService(repoState: TrieMap[Id[Tweet], Tweet]): TweetService[CatsId] = {
    val tweetRepository = LocalTweetRepository.create[CatsId](repoState)
    val authByAuthorService = byAuthor(tweetRepository)
    TweetService.create[CatsId, CatsId](tweetRepository, authByAuthorService)
  }

}

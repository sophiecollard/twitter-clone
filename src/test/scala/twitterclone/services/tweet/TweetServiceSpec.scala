package twitterclone.services.tweet

import cats.{Id => CatsId}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import twitterclone.auth.error.AuthorizationError.NotTheTweetsAuthor
import twitterclone.model.{Id, Tweet, User}
import twitterclone.repositories.tweet.LocalTweetRepository
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
        }
      }
    }

    "the specified tweet id doesn't exist" should {
      "return an error" in new Fixtures {
        private val repoState = TrieMap.from((tweet.id, tweet) :: Nil)
        private val service = newService(repoState)
        private val randomTweetId = Id.random[Tweet]

        withFailedAuthorization(service.delete(randomTweetId)(userId)) { error =>
          error shouldBe NotTheTweetsAuthor(userId, randomTweetId)
        }
      }
    }
  }
}

trait Fixtures {

  val userId: Id[User] = Id.random[User]

  val tweet: Tweet = Tweet(
    id = Id.random[Tweet],
    author = userId,
    contents =
      "Mieux vaut mobiliser son intelligence sur des betises que mobiliser sa betise sur des choses intelligentes.",
    postedOn = LocalDateTime.of(
      LocalDate.of(1968, 4, 29),
      LocalTime.of(19, 30)
    )
  )

  val contents: String =
    "Mieux vaut mobiliser son intelligence sur des betises que mobiliser sa betise sur des choses intelligentes."

  def newService(repoState: TrieMap[Id[Tweet], Tweet]): TweetService[CatsId] = {
    val tweetRepository = LocalTweetRepository.create[CatsId](repoState)
    val authByAuthorService = byAuthor(tweetRepository)
    TweetService.create[CatsId, CatsId](tweetRepository, authByAuthorService)
  }

}

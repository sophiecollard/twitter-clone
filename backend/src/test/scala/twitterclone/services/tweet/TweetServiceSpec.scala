package twitterclone.services.tweet

import cats.{Id => CatsId}
import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import twitterclone.auth.error.AuthorizationError.NotTheTweetsAuthor
import twitterclone.fixtures.tweet._
import twitterclone.model.user.User
import twitterclone.model.{Id, Pagination, Tweet}
import twitterclone.repositories.domain.TweetRepository.TweetData
import twitterclone.repositories.interpreters.local.{LocalLikeRepository, LocalTweetRepository}
import twitterclone.services.error.ServiceError.ResourceNotFound
import twitterclone.services.tweet.auth.byAuthor
import twitterclone.testinstances._
import twitterclone.testsyntax._

import scala.collection.concurrent.TrieMap

class TweetServiceSpec extends AnyWordSpec with Matchers with OptionValues {
  "The create method" should {
    "create and return a new tweet" in new Fixtures {
      private val repoState = TrieMap.empty[Id[Tweet], TweetData]
      private val service = newService(repoState)
      private val userId = Id.random[User]
      private val contents: String =
        "Mieux vaut mobiliser son intelligence sur des betises que mobiliser sa betise sur des choses intelligentes."

      withNoServiceError(service.create(contents)(userId)) { tweet =>
        tweet.authorId shouldBe userId
        tweet.contents shouldBe contents
        val storedData = repoState.get(tweet.id).value
        storedData.authorId shouldBe userId
        storedData.contents shouldBe tweetData.contents
      }
    }
  }

  "The delete method" when {
    "the user making the request is the tweet's author" should {
      "delete the tweet" in new Fixtures {
        private val repoState = TrieMap.from((tweet.id, tweetData) :: Nil)
        private val service = newService(repoState)

        withSuccessfulAuthorization(service.delete(tweet.id)(tweet.authorId)) {
          withNoServiceError(_) { _ =>
            repoState.get(tweet.id) shouldBe None
          }
        }
      }
    }

    "the user making the request is not the tweet's author" should {
      "return an error" in new Fixtures {
        private val repoState = TrieMap.from((tweet.id, tweetData) :: Nil)
        private val service = newService(repoState)
        private val randomUserId = Id.random[User]

        withFailedAuthorization(service.delete(tweet.id)(randomUserId)) { error =>
          error shouldBe NotTheTweetsAuthor(randomUserId, tweet.id)
          assert(repoState.contains(tweet.id))
        }
      }
    }

    "the specified tweet id doesn't exist" should {
      "return an error" in new Fixtures {
        private val repoState = TrieMap.from((tweet.id, tweetData) :: Nil)
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
        private val repoState = TrieMap.from((tweet.id, tweetData) :: Nil)
        private val service = newService(repoState)

        withNoServiceError(service.get(tweet.id)(userId = None)) { returnedTweet =>
          returnedTweet shouldBe tweet
        }
      }
    }

    "the specified tweet id doesn't exist" should {
      "return an error" in new Fixtures {
        private val repoState = TrieMap.from((tweet.id, tweetData) :: Nil)
        private val service = newService(repoState)
        private val randomTweetId = Id.random[Tweet]

        withServiceError(service.get(randomTweetId)(userId = None)) { error =>
          error shouldBe ResourceNotFound(randomTweetId, "Tweet")
        }
      }
    }
  }

  "The list method" should {
    "return a list of tweets" in new Fixtures {
      private val repoState = TrieMap.from(
        (tweet.id, tweetData) ::
          (earlierTweetFromSameAuthor.id, earlierTweetFromSameAuthorData) ::
          (tweetFromAnotherAuthor.id, tweetFromAnotherAuthorData) :: Nil)
      private val service = newService(repoState)

      service.list()(userId = None) shouldEqual List(tweet, earlierTweetFromSameAuthor, tweetFromAnotherAuthor)
    }

    "only return as many tweets as specified in the pagination" in new Fixtures {
      private val repoState = TrieMap.from(
        (tweet.id, tweetData) ::
          (earlierTweetFromSameAuthor.id, earlierTweetFromSameAuthorData) ::
          (tweetFromAnotherAuthor.id, tweetFromAnotherAuthorData) :: Nil)
      private val service = newService(repoState)
      private val pagination = Pagination(pageSize = 1, postedBefore = None)

      service.list(pagination)(userId = None) shouldEqual List(tweet)
    }

    "only return tweets posted before the date specified in the pagination" in new Fixtures {
      private val repoState = TrieMap.from(
        (tweet.id, tweetData) ::
          (earlierTweetFromSameAuthor.id, earlierTweetFromSameAuthorData) ::
          (tweetFromAnotherAuthor.id, tweetFromAnotherAuthorData) :: Nil)
      private val service = newService(repoState)
      private val pagination = Pagination(pageSize = 10, postedBefore = Some(tweet.postedOn))

      service.list(pagination)(userId = None) shouldEqual List(earlierTweetFromSameAuthor, tweetFromAnotherAuthor)
    }
  }

  "The listBy method" when {
    "given an author id" should {
      "return a list of tweets from this author" in new Fixtures {
        private val repoState = TrieMap.from(
          (tweet.id, tweetData) ::
            (earlierTweetFromSameAuthor.id, earlierTweetFromSameAuthorData) ::
            (tweetFromAnotherAuthor.id, tweetFromAnotherAuthorData) :: Nil)
        private val service = newService(repoState)

        val tweets: List[Tweet] = service.listBy(tweet.authorId)(userId = None)
        tweets.size shouldBe 2
        tweets should contain theSameElementsAs List(tweet, earlierTweetFromSameAuthor)
        tweets should not contain tweetFromAnotherAuthor
      }

      "only return as many tweets as specified in the pagination" in new Fixtures {
        private val repoState = TrieMap.from(
          (tweet.id, tweetData) ::
            (earlierTweetFromSameAuthor.id, earlierTweetFromSameAuthorData) ::
            (tweetFromAnotherAuthor.id, tweetFromAnotherAuthorData) :: Nil)
        private val service = newService(repoState)
        private val pagination = Pagination(pageSize = 1, postedBefore = None)

        val tweets: List[Tweet] = service.listBy(tweet.authorId, pagination)(userId = None)
        tweets.size shouldBe 1
        tweets should contain (tweet)
      }

      "only return tweets posted before the date specified in the pagination" in new Fixtures {
        private val repoState = TrieMap.from(
          (tweet.id, tweetData) ::
            (earlierTweetFromSameAuthor.id, earlierTweetFromSameAuthorData) ::
            (tweetFromAnotherAuthor.id, tweetFromAnotherAuthorData) :: Nil)
        private val service = newService(repoState)
        private val pagination = Pagination(pageSize = 10, postedBefore = Some(tweet.postedOn))

        val tweets: List[Tweet] = service.listBy(tweet.authorId, pagination)(userId = None)
        tweets.size shouldBe 1
        tweets should contain (earlierTweetFromSameAuthor)
        tweets should not contain tweet
        tweets should not contain tweetFromAnotherAuthor
      }
    }

    "no tweets exist from the specified author" should {
      "return an empty list" in new Fixtures {
        private val repoState = TrieMap.from(
          (tweet.id, tweetData) ::
            (earlierTweetFromSameAuthor.id, earlierTweetFromSameAuthorData) ::
            (tweetFromAnotherAuthor.id, tweetFromAnotherAuthorData) :: Nil)
        private val service = newService(repoState)
        private val randomUserId = Id.random[User]

        private val tweets = service.listBy(randomUserId)(userId = None)
        tweets shouldBe empty
      }
    }
  }
}

trait Fixtures {

  def newService(repoState: TrieMap[Id[Tweet], TweetData]): TweetService[CatsId] = {
    val tweetRepository = LocalTweetRepository.create[CatsId](repoState)
    val likeRepository = LocalLikeRepository[CatsId]() // FIXME
    val authByAuthorService = byAuthor(tweetRepository)
    TweetService.create[CatsId, CatsId](tweetRepository, likeRepository, authByAuthorService)
  }

}

package twitterclone.repositories.interpreters.local

import cats.{Id => CatsId}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import twitterclone.fixtures.tweet._
import twitterclone.model.{Id, Tweet, TweetPagination}

import scala.collection.concurrent.TrieMap

class LocalTweetRepositorySpec extends AnyWordSpec with Matchers {
  "The create method" should {
    "create a new tweet and return 1" in {
      val state = TrieMap.empty[Id[Tweet], Tweet]
      val repo = LocalTweetRepository.create[CatsId](state)
      repo.create(tweet) shouldBe 1
      state.get(tweet.id) shouldBe Some(tweet)
    }

    "not override an existing tweet" in {
      val state = TrieMap.from((tweet.id, tweet) :: Nil)
      val repo = LocalTweetRepository.create[CatsId](state)
      val tweetWithSameId = tweetFromAnotherAuthor.copy(id = tweet.id)
      repo.create(tweetWithSameId) shouldBe 0
      repo.get(tweet.id) shouldBe Some(tweet)
    }
  }

  "The delete method" should {
    "delete a tweet and return 1" in {
      val state = TrieMap.from((tweet.id, tweet) :: Nil)
      val repo = LocalTweetRepository.create[CatsId](state)
      repo.delete(tweet.id) shouldBe 1
      state.contains(tweet.id) shouldBe false
    }
  }

  "The get method" should {
    "get a tweet" in {
      val state = TrieMap.from((tweet.id, tweet) :: Nil)
      val repo = LocalTweetRepository.create[CatsId](state)
      repo.get(tweet.id) shouldBe Some(tweet)
    }
  }

  "The getAuthor method" should {
    "get a tweet author's user Id" in {
      val state = TrieMap.from((tweet.id, tweet) :: Nil)
      val repo = LocalTweetRepository.create[CatsId](state)
      repo.getAuthor(tweet.id) shouldBe Some(tweet.author)
    }
  }

  "The list method" should {
    "list tweets by decreasing 'postedOn' date" in {
      val state = TrieMap.from(
        (tweet.id, tweet) ::
          (earlierTweetFromSameAuthor.id, earlierTweetFromSameAuthor) ::
          (tweetFromAnotherAuthor.id, tweetFromAnotherAuthor) ::
          Nil
      )
      val repo = LocalTweetRepository.create[CatsId](state)
      val pagination = TweetPagination.default
      repo.list(pagination) shouldBe List(tweet, earlierTweetFromSameAuthor, tweetFromAnotherAuthor)
    }
  }

  "The listBy method" should {
    "list tweets by a given author by decreasing 'postedOn' date" in {
      val state = TrieMap.from(
        (tweet.id, tweet) ::
          (earlierTweetFromSameAuthor.id, earlierTweetFromSameAuthor) ::
          (tweetFromAnotherAuthor.id, tweetFromAnotherAuthor) ::
          Nil
      )
      val repo = LocalTweetRepository.create[CatsId](state)
      val pagination = TweetPagination.default
      repo.listBy(tweet.author, pagination) shouldBe List(tweet, earlierTweetFromSameAuthor)
    }
  }
}

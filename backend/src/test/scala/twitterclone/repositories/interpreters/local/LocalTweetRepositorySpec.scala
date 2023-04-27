package twitterclone.repositories.interpreters.local

import cats.{Id => CatsId}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import twitterclone.fixtures.tweet._
import twitterclone.model.{Id, Pagination, Tweet}
import twitterclone.repositories.domain.TweetRepository.TweetData

import scala.collection.concurrent.TrieMap

class LocalTweetRepositorySpec extends AnyWordSpec with Matchers {
  "The create method" should {
    "create a new tweet and return 1" in {
      val state = TrieMap.empty[Id[Tweet], TweetData]
      val repo = LocalTweetRepository.create[CatsId](state)
      repo.create(tweetData) shouldBe 1
      state.get(tweet.id) shouldBe Some(tweetData)
    }

    "not override an existing tweet" in {
      val state = TrieMap.from((tweet.id, tweetData) :: Nil)
      val repo = LocalTweetRepository.create[CatsId](state)
      val tweetWithSameIdData = tweetFromAnotherAuthorData.copy(id = tweet.id)
      repo.create(tweetWithSameIdData) shouldBe 0
      repo.get(tweet.id) shouldBe Some(tweetData)
    }
  }

  "The delete method" should {
    "delete a tweet and return 1" in {
      val state = TrieMap.from((tweet.id, tweetData) :: Nil)
      val repo = LocalTweetRepository.create[CatsId](state)
      repo.delete(tweet.id) shouldBe 1
      state.contains(tweet.id) shouldBe false
    }
  }

  "The get method" should {
    "get a tweet" in {
      val state = TrieMap.from((tweet.id, tweetData) :: Nil)
      val repo = LocalTweetRepository.create[CatsId](state)
      repo.get(tweet.id) shouldBe Some(tweetData)
    }
  }

  "The getAuthor method" should {
    "get a tweet author's user id" in {
      val state = TrieMap.from((tweet.id, tweetData) :: Nil)
      val repo = LocalTweetRepository.create[CatsId](state)
      repo.getAuthorId(tweet.id) shouldBe Some(tweetData.authorId)
    }
  }

  "The list method" should {
    "list tweets by decreasing 'postedOn' timestamp" in {
      val state = TrieMap.from(
        (tweet.id, tweetData) ::
          (earlierTweetFromSameAuthor.id, earlierTweetFromSameAuthorData) ::
          (tweetFromAnotherAuthor.id, tweetFromAnotherAuthorData) ::
          Nil
      )
      val repo = LocalTweetRepository.create[CatsId](state)
      val pagination = Pagination.default
      repo.list(pagination) shouldBe List(tweetData, earlierTweetFromSameAuthorData, tweetFromAnotherAuthorData)
    }
  }

  "The listBy method" should {
    "list tweets by a given author by decreasing 'postedOn' timestamp" in {
      val state = TrieMap.from(
        (tweet.id, tweetData) ::
          (earlierTweetFromSameAuthor.id, earlierTweetFromSameAuthorData) ::
          (tweetFromAnotherAuthor.id, tweetFromAnotherAuthorData) ::
          Nil
      )
      val repo = LocalTweetRepository.create[CatsId](state)
      val pagination = Pagination.default
      repo.listBy(tweet.authorId, pagination) shouldBe List(tweetData, earlierTweetFromSameAuthorData)
    }
  }
}

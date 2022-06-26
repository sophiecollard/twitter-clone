package twitterclone.repositories.tweet

import cats.{Id => CatsId}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import twitterclone.model.{Id, Tweet, TweetPagination, User}

import java.time.{LocalDate, LocalDateTime, LocalTime}
import scala.collection.concurrent.TrieMap

class TweetRepositorySpec extends AnyWordSpec with Matchers {
  "The local implementation" should {
    "create a new tweet" in new Fixtures {
      private val state = TrieMap.empty[Id[Tweet], Tweet]
      private val repo = TweetRepository.local[CatsId](state)
      repo.create(tweet)
      state.get(tweet.id) shouldBe Some(tweet)
    }

    "delete a tweet" in new Fixtures {
      private val state = TrieMap.from((tweet.id, tweet) :: Nil)
      private val repo = TweetRepository.local[CatsId](state)
      repo.delete(tweet.id)
      state.contains(tweet.id) shouldBe false
    }

    "get a tweet" in new Fixtures {
      private val state = TrieMap.from((tweet.id, tweet) :: Nil)
      private val repo = TweetRepository.local[CatsId](state)
      repo.get(tweet.id) shouldBe Some(tweet)
    }

    "get a tweet's author" in new Fixtures {
      private val state = TrieMap.from((tweet.id, tweet) :: Nil)
      private val repo = TweetRepository.local[CatsId](state)
      repo.getAuthor(tweet.id) shouldBe Some(tweet.author)
    }

    "list tweets for an author" in new Fixtures {
      private val state = TrieMap.from(
        (tweet.id, tweet) ::
          (tweetFromSameAuthor.id, tweetFromSameAuthor) ::
          (tweetFromAnotherAuthor.id, tweetFromAnotherAuthor) ::
          Nil
      )
      private val repo = TweetRepository.local[CatsId](state)
      repo.list(tweet.author, pagination) shouldBe List(tweetFromSameAuthor, tweet)
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
      LocalDate.of(1968, 4, 29),
      LocalTime.of(19, 30)
    )
  )

  val tweetFromSameAuthor: Tweet = Tweet(
    id = Id.random[Tweet],
    author = tweet.author,
    contents = "Je dis des choses tellement intelligentes que souvent, je ne comprends pas ce que je dis.",
    postedOn = LocalDateTime.of(
      LocalDate.of(1968, 4, 30),
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

  val pagination: TweetPagination = TweetPagination.default
}

package twitterclone.repositories.comment

import cats.{Id => CatsId}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import twitterclone.model.{Comment, CommentPagination, Id, Tweet, User}

import java.time.{LocalDate, LocalTime, ZoneId, ZonedDateTime}
import scala.collection.concurrent.TrieMap

class CommentRepositorySpec extends AnyWordSpec with Matchers {
  "The local implementation" should {
    "create a new comment" in new Fixtures {
      private val state = TrieMap.empty[Id[Comment], Comment]
      private val repo = CommentRepository.local[CatsId](state)
      repo.create(comment)
      state.get(comment.id) shouldBe Some(comment)
    }

    "delete a comment" in new Fixtures {
      private val state = TrieMap.from((comment.id, comment) :: Nil)
      private val repo = CommentRepository.local[CatsId](state)
      repo.delete(comment.id)
      state.contains(comment.id) shouldBe false
    }

    "get a comment" in new Fixtures {
      private val state = TrieMap.from((comment.id, comment) :: Nil)
      private val repo = CommentRepository.local[CatsId](state)
      repo.get(comment.id) shouldBe Some(comment)
    }

    "get a comment's author" in new Fixtures {
      private val state = TrieMap.from((comment.id, comment) :: Nil)
      private val repo = CommentRepository.local[CatsId](state)
      repo.getAuthor(comment.id) shouldBe Some(comment.author)
    }

    "list comments for a tweet" in new Fixtures {
      private val state = TrieMap.from(
        (comment.id, comment) ::
          (commentOnSameTweet.id, commentOnSameTweet) ::
          (commentFromSameAuthor.id, commentFromSameAuthor) ::
          Nil
      )
      private val repo = CommentRepository.local[CatsId](state)
      repo.list(comment.tweetId, pagination) shouldBe List(commentOnSameTweet, comment)
    }
  }
}

trait Fixtures {
  val comment: Comment = Comment(
    id = Id.random[Comment],
    author = Id.random[User],
    tweetId = Id.random[Tweet],
    contents =
      "Mieux vaut mobiliser son intelligence sur des betises que mobiliser sa betise sur des choses intelligentes.",
    postedOn = ZonedDateTime.of(
      LocalDate.of(1968, 4, 29),
      LocalTime.of(19, 30),
      ZoneId.of("Europe/Paris")
    )
  )

  val commentOnSameTweet: Comment = Comment(
    id = Id.random[Comment],
    author = Id.random[User],
    tweetId = comment.tweetId,
    contents = "Je dis des choses tellement intelligentes que souvent, je ne comprends pas ce que je dis.",
    postedOn = ZonedDateTime.of(
      LocalDate.of(1968, 4, 30),
      LocalTime.of(19, 30),
      ZoneId.of("Europe/Paris")
    )
  )

  val commentFromSameAuthor: Comment = Comment(
    id = Id.random[Comment],
    author = comment.author,
    tweetId = Id.random[Tweet],
    contents = "S'il n'a a pas de solution, c'est qu'il n'y a pas de problème.",
    postedOn = ZonedDateTime.of(
      LocalDate.of(1968, 1, 1),
      LocalTime.of(19, 30),
      ZoneId.of("Europe/Paris")
    )
  )

  val pagination: CommentPagination = CommentPagination(
    pageSize = 10,
    postedAfter = ZonedDateTime.of(
      LocalDate.of(1968, 1, 1),
      LocalTime.of(0, 0),
      ZoneId.of("Europe/Paris")
    )
  )
}

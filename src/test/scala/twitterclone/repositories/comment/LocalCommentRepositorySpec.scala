package twitterclone.repositories.comment

import cats.{Id => CatsId}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import twitterclone.model.{Comment, CommentPagination, Id, Tweet, User}

import java.time.{LocalDate, LocalDateTime, LocalTime}
import scala.collection.concurrent.TrieMap

class LocalCommentRepositorySpec extends AnyWordSpec with Matchers {
  "The create method" should {
    "create a new comment and return 1" in new Fixtures {
      private val state = TrieMap.empty[Id[Comment], Comment]
      private val repo = LocalCommentRepository.create[CatsId](state)
      repo.create(comment) shouldBe 1
      state.get(comment.id) shouldBe Some(comment)
    }

    "not override a comment" in new Fixtures {
      private val state = TrieMap.from((comment.id, comment) :: Nil)
      private val repo = LocalCommentRepository.create[CatsId](state)
      private val commentWithSameId = commentOnAnotherTweet.copy(id = comment.id)
      repo.create(commentWithSameId) shouldBe 0
      state.get(comment.id) shouldBe Some(comment)
    }
  }

  "The delete method" should {
    "delete a comment and return 1" in new Fixtures {
      private val state = TrieMap.from((comment.id, comment) :: Nil)
      private val repo = LocalCommentRepository.create[CatsId](state)
      repo.delete(comment.id) shouldBe 1
      state.contains(comment.id) shouldBe false
    }
  }

  "The get method" should {
    "get a comment" in new Fixtures {
      private val state = TrieMap.from((comment.id, comment) :: Nil)
      private val repo = LocalCommentRepository.create[CatsId](state)
      repo.get(comment.id) shouldBe Some(comment)
    }
  }

  "The getAuthor method" should {
    "get a comment author's user Id" in new Fixtures {
      private val state = TrieMap.from((comment.id, comment) :: Nil)
      private val repo = LocalCommentRepository.create[CatsId](state)
      repo.getAuthor(comment.id) shouldBe Some(comment.author)
    }
  }


  "The list method" should {
    "list comments for a tweet" in new Fixtures {
      private val state = TrieMap.from(
        (comment.id, comment) ::
          (earlierCommentOnSameTweet.id, earlierCommentOnSameTweet) ::
          (commentOnAnotherTweet.id, commentOnAnotherTweet) ::
          Nil
      )
      private val repo = LocalCommentRepository.create[CatsId](state)
      repo.list(comment.tweetId, pagination) shouldBe List(comment, earlierCommentOnSameTweet)
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
    postedOn = LocalDateTime.of(
      LocalDate.of(1968, 4, 30),
      LocalTime.of(19, 30)
    )
  )

  val earlierCommentOnSameTweet: Comment = Comment(
    id = Id.random[Comment],
    author = Id.random[User],
    tweetId = comment.tweetId,
    contents = "Je dis des choses tellement intelligentes que souvent, je ne comprends pas ce que je dis.",
    postedOn = LocalDateTime.of(
      LocalDate.of(1968, 4, 29),
      LocalTime.of(19, 30)
    )
  )

  val commentOnAnotherTweet: Comment = Comment(
    id = Id.random[Comment],
    author = comment.author,
    tweetId = Id.random[Tweet],
    contents = "S'il n'a a pas de solution, c'est qu'il n'y a pas de problème.",
    postedOn = LocalDateTime.of(
      LocalDate.of(1968, 1, 1),
      LocalTime.of(19, 30)
    )
  )

  val pagination: CommentPagination = CommentPagination.default
}

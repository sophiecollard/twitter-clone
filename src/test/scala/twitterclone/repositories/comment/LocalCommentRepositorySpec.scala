package twitterclone.repositories.comment

import cats.{Id => CatsId}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import twitterclone.fixtures.comment._
import twitterclone.model.{Comment, CommentPagination, Id}

import scala.collection.concurrent.TrieMap

class LocalCommentRepositorySpec extends AnyWordSpec with Matchers {
  "The create method" should {
    "create a new comment and return 1" in {
      val state = TrieMap.empty[Id[Comment], Comment]
      val repo = LocalCommentRepository.create[CatsId](state)
      repo.create(comment) shouldBe 1
      state.get(comment.id) shouldBe Some(comment)
    }

    "not override a comment" in {
      val state = TrieMap.from((comment.id, comment) :: Nil)
      val repo = LocalCommentRepository.create[CatsId](state)
      val commentWithSameId = commentOnAnotherTweet.copy(id = comment.id)
      repo.create(commentWithSameId) shouldBe 0
      state.get(comment.id) shouldBe Some(comment)
    }
  }

  "The delete method" should {
    "delete a comment and return 1" in {
      val state = TrieMap.from((comment.id, comment) :: Nil)
      val repo = LocalCommentRepository.create[CatsId](state)
      repo.delete(comment.id) shouldBe 1
      state.contains(comment.id) shouldBe false
    }
  }

  "The get method" should {
    "get a comment" in {
      val state = TrieMap.from((comment.id, comment) :: Nil)
      val repo = LocalCommentRepository.create[CatsId](state)
      repo.get(comment.id) shouldBe Some(comment)
    }
  }

  "The getAuthor method" should {
    "get a comment author's user Id" in {
      val state = TrieMap.from((comment.id, comment) :: Nil)
      val repo = LocalCommentRepository.create[CatsId](state)
      repo.getAuthor(comment.id) shouldBe Some(comment.author)
    }
  }


  "The list method" should {
    "list comments for a tweet" in {
      val state = TrieMap.from(
        (comment.id, comment) ::
          (earlierCommentOnSameTweet.id, earlierCommentOnSameTweet) ::
          (commentOnAnotherTweet.id, commentOnAnotherTweet) ::
          Nil
      )
      val repo = LocalCommentRepository.create[CatsId](state)
      val pagination = CommentPagination.default
      repo.list(comment.tweetId, pagination) shouldBe List(comment, earlierCommentOnSameTweet)
    }
  }
}

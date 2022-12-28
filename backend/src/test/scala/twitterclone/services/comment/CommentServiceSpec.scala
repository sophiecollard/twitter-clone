package twitterclone.services.comment

import cats.{Id => CatsId}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import twitterclone.auth.error.AuthorizationError.NotTheCommentsAuthor
import twitterclone.fixtures.comment._
import twitterclone.model.user.User
import twitterclone.model.{Comment, CommentPagination, Id, Tweet}
import twitterclone.repositories.interpreters.local.LocalCommentRepository
import twitterclone.services.comment.auth.byAuthor
import twitterclone.services.error.ServiceError.ResourceNotFound
import twitterclone.testinstances._
import twitterclone.testsyntax._

import scala.collection.concurrent.TrieMap

class CommentServiceSpec extends AnyWordSpec with Matchers {
  "The create method" should {
    "create and return a new comment" in new Fixtures {
      private val repoState = TrieMap.empty[Id[Comment], Comment]
      private val service = newService(repoState)
      private val userId = Id.random[User]
      private val tweetId = Id.random[Tweet]
      private val contents: String =
        "Mieux vaut mobiliser son intelligence sur des betises que mobiliser sa betise sur des choses intelligentes."

      withNoServiceError(service.create(tweetId, contents)(userId)) { comment =>
        comment.authorId shouldBe userId
        comment.tweetId shouldBe tweetId
        comment.contents shouldBe contents
        repoState.get(comment.id) shouldBe Some(comment)
      }
    }
  }

  "The delete method" when {
    "the user making the request is the comment's author" should {
      "delete the comment" in new Fixtures {
        private val repoState = TrieMap.from((comment.id, comment) :: Nil)
        private val service = newService(repoState)

        withSuccessfulAuthorization(service.delete(comment.id)(comment.authorId)) {
          withNoServiceError(_) { _ =>
            repoState.get(comment.id) shouldBe None
          }
        }
      }
    }

    "the user making the request is not the comment's author" should {
      "return an error" in new Fixtures {
        private val repoState = TrieMap.from((comment.id, comment) :: Nil)
        private val service = newService(repoState)
        private val randomUserId = Id.random[User]

        withFailedAuthorization(service.delete(comment.id)(randomUserId)) { error =>
          error shouldBe NotTheCommentsAuthor(randomUserId, comment.id)
          repoState.get(comment.id) shouldBe Some(comment)
        }
      }
    }

    "the specified comment id doesn't exist" should {
      "return an error" in new Fixtures {
        private val repoState = TrieMap.from((comment.id, comment) :: Nil)
        private val service = newService(repoState)
        private val userId = Id.random[User]
        private val randomCommentId = Id.random[Comment]

        withFailedAuthorization(service.delete(randomCommentId)(userId)) { error =>
          error shouldBe NotTheCommentsAuthor(userId, randomCommentId)
        }
      }
    }
  }

  "The get method" when {
    "the specified comment id exists" should {
      "get the comment" in new Fixtures {
        private val repoState = TrieMap.from((comment.id, comment) :: Nil)
        private val service = newService(repoState)

        withNoServiceError(service.get(comment.id)) { returnedComment =>
          returnedComment shouldBe comment
        }
      }
    }

    "the specified comment id doesn't exist" should {
      "return an error" in new Fixtures {
        private val repoState = TrieMap.from((comment.id, comment) :: Nil)
        private val service = newService(repoState)
        private val randomCommentId = Id.random[Comment]

        withServiceError(service.get(randomCommentId)) { error =>
          error shouldBe ResourceNotFound(randomCommentId, "Comment")
        }
      }
    }
  }

  "The list method" when {
    "given a tweet id" should {
      "return a list of comments for this tweet" in new Fixtures {
        private val repoState = TrieMap.from(
          (comment.id, comment) ::
            (earlierCommentOnSameTweet.id, earlierCommentOnSameTweet) ::
            (commentOnAnotherTweet.id, commentOnAnotherTweet) :: Nil)
        private val service = newService(repoState)

        withNoServiceError(service.list(comment.tweetId)) { comments =>
          comments.size shouldBe 2
          comments should contain theSameElementsAs List(comment, earlierCommentOnSameTweet)
          comments should not contain commentOnAnotherTweet
        }
      }

      "only return as many comments as specified in the pagination" in new Fixtures {
        private val repoState = TrieMap.from(
          (comment.id, comment) ::
            (earlierCommentOnSameTweet.id, earlierCommentOnSameTweet) ::
            (commentOnAnotherTweet.id, commentOnAnotherTweet) :: Nil)
        private val service = newService(repoState)
        private val pagination = CommentPagination(pageSize = 1, postedBefore = None)

        withNoServiceError(service.list(comment.tweetId, pagination)) { comments =>
          comments.size shouldBe 1
          comments should contain (comment)
        }
      }

      "only return comments posted before the date specified in the pagination" in new Fixtures {
        private val repoState = TrieMap.from(
          (comment.id, comment) ::
            (earlierCommentOnSameTweet.id, earlierCommentOnSameTweet) ::
            (commentOnAnotherTweet.id, commentOnAnotherTweet) :: Nil)
        private val service = newService(repoState)
        private val pagination = CommentPagination(pageSize = 10, postedBefore = Some(comment.postedOn))

        withNoServiceError(service.list(comment.tweetId, pagination)) { comments =>
          comments.size shouldBe 1
          comments should contain (earlierCommentOnSameTweet)
          comments should not contain comment
          comments should not contain commentOnAnotherTweet
        }
      }
    }

    "no comments exist for the specified tweet" should {
      "return an empty list" in new Fixtures {
        private val repoState = TrieMap.from(
          (comment.id, comment) ::
            (earlierCommentOnSameTweet.id, earlierCommentOnSameTweet) ::
            (commentOnAnotherTweet.id, commentOnAnotherTweet) :: Nil)
        private val service = newService(repoState)
        private val randomTweetId = Id.random[Tweet]

        withNoServiceError(service.list(randomTweetId)) { comments =>
          comments shouldBe empty
        }
      }
    }
  }
}

trait Fixtures {

  def newService(repoState: TrieMap[Id[Comment], Comment]): CommentService[CatsId] = {
    val commentRepository = LocalCommentRepository.create[CatsId](repoState)
    val authByAuthorService = byAuthor(commentRepository)
    CommentService.create[CatsId, CatsId](commentRepository, authByAuthorService)
  }

}

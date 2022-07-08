package twitterclone.api.comment

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto
import org.http4s.server.AuthMiddleware
import org.scalatest.EitherValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import twitterclone.api.authentication.dummyAuthMiddleware
import twitterclone.instances.ioTransactor
import twitterclone.model.{Comment, Id, User}
import twitterclone.repositories.comment.LocalCommentRepository
import twitterclone.services.comment.CommentService
import twitterclone.services.comment.auth.byAuthor

import scala.collection.concurrent.TrieMap

class CommentEndpointsSpec extends AnyWordSpec with EitherValues with Matchers {
  "The POST /v1/comments endpoint" when {
    "the user is authenticated" should {
      "create and return a new comment" in pending
    }

    "the user is not authenticated" should {
      "respond with an unauthorized error" in pending
    }
  }

  "The DELETE /v1/comments/{comment_id} endpoint" when {
    "the user is authenticated and is the author of the comment" should {
      "delete the comment" in pending
    }

    "the user is authenticated but is not the author of the comment" should {
      "respond with an unauthorized error" in pending
    }

    "the user is not authenticated" should {
      "respond with an unauthorized error" in pending
    }
  }

  "The GET /v1/comments/{comment_id} endpoint" should {
    "return the comment with the specified id" in pending
  }

  "The GET /v1/comments?tweet-id={tweet_id}" should {
    "return a list of comments for the specified tweet" in pending
  }
}

trait Fixtures {

  implicit val runtime: IORuntime = cats.effect.unsafe.IORuntime.global

  val authMiddleware: AuthMiddleware[IO, Id[User]] = dummyAuthMiddleware

  def newEndpoints(repoState: TrieMap[Id[Comment], Comment]): CommentEndpoints[IO] = {
    val commentRepository = LocalCommentRepository.create[IO](repoState)
    val authByAuthorService = byAuthor(commentRepository)
    val commentService = CommentService.create[IO, IO](commentRepository, authByAuthorService)
    CommentEndpoints.create(dummyAuthMiddleware[IO], commentService)
  }

  import twitterclone.api.shared.instances._

  implicit val newCommentRequestBodyEncoder: Encoder[NewCommentRequestBody] =
    semiauto.deriveEncoder

  implicit val commentDecoder: Decoder[Comment] =
    semiauto.deriveDecoder

}

package twitterclone.api.v2.interpreters

import cats.effect.kernel.Async
import cats.implicits._
import org.http4s.HttpRoutes
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter
import twitterclone.api.error.ApiError
import twitterclone.api.v2.domain.CommentApiEndpoints
import twitterclone.auth.{Failure, Success}
import twitterclone.services.comment.CommentService

final case class Http4sCommentApiEndpoints[F[_]](httpRoutes: HttpRoutes[F])

object Http4sCommentApiEndpoints {

  def apply[F[_]: Async](commentService: CommentService[F]): Http4sCommentApiEndpoints[F] = {

    val postCommentEndpoint: ServerEndpoint[Any, F] =
      CommentApiEndpoints
        .postCommentEndpoint
        .serverSecurityLogicPure(_.asRight)
        .serverLogic { userId => request =>
          commentService
            .create(tweetId = request.tweetId, request.contents)(userId)
            .map(_.leftMap(ApiError.fromServiceError))
        }

    val deleteCommentEndpoint: ServerEndpoint[Any, F] =
      CommentApiEndpoints
        .deleteCommentEndpoint
        .serverSecurityLogicPure(_.asRight) // FIXME Don't this in a real-world app!
        .serverLogic { userId => id =>
          commentService.delete(id)(userId).map {
            case Success(result) => result.leftMap(ApiError.fromServiceError)
            case Failure(error)  => Left(ApiError.fromAuthorizationError(error))
          }
        }

    val getCommentEndpoint: ServerEndpoint[Any, F] =
      CommentApiEndpoints
        .getCommentEndpoint
        .serverLogic(id => commentService.get(id).map(_.leftMap(ApiError.fromServiceError)))

    val listCommentsEndpoint: ServerEndpoint[Any, F] =
      CommentApiEndpoints
        .listCommentsEndpoint
        .serverLogic { case (tweetId, pagination) =>
          commentService.list(tweetId, pagination).map(_.leftMap(ApiError.fromServiceError))
        }

    val publicRoutes: HttpRoutes[F] =
      Http4sServerInterpreter[F]()
        .toRoutes(postCommentEndpoint :: deleteCommentEndpoint :: getCommentEndpoint :: listCommentsEndpoint :: Nil)

    Http4sCommentApiEndpoints(publicRoutes)
  }

}

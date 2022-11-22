package twitterclone.api.v2.interpreters

import cats.effect.kernel.Async
import cats.implicits._
import org.http4s.HttpRoutes
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter
import twitterclone.api.error.ApiError
import twitterclone.api.v2.domain.CommentEndpoints
import twitterclone.services.comment.CommentService

final case class Http4sCommentEndpoints[F[_]](httpRoutes: HttpRoutes[F])

object Http4sCommentEndpoints {

  def create[F[_]: Async](commentService: CommentService[F]): Http4sCommentEndpoints[F] = {

    val getCommentEndpoint: ServerEndpoint[Any, F] =
      CommentEndpoints
        .getCommentEndpoint
        .serverLogic(id => commentService.get(id).map(_.leftMap(ApiError.fromServiceError)))

    val publicRoutes: HttpRoutes[F] =
      Http4sServerInterpreter[F]().toRoutes(getCommentEndpoint :: Nil)

    Http4sCommentEndpoints(publicRoutes)
  }

}

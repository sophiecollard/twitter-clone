package twitterclone.api.v2.interpreters

import cats.effect.kernel.Async
import cats.implicits._
import org.http4s.HttpRoutes
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter
import twitterclone.api.error.ApiError
import twitterclone.api.v2.domain.UserApiEndpoints
import twitterclone.services.user.UserService

final case class Http4sUserApiEndpoints[F[_]](httpRoutes: HttpRoutes[F])

object Http4sUserApiEndpoints {

  def apply[F[_] : Async](userService: UserService[F]): Http4sUserApiEndpoints[F] = {

    val createUserEndpoint: ServerEndpoint[Any, F] =
      UserApiEndpoints.createUserEndpoint.serverLogic { request =>
        userService.create(request.handle, request.name)
          .map(_.leftMap(ApiError.fromServiceError))
      }

    val publicRoutes: HttpRoutes[F] =
      Http4sServerInterpreter[F]()
        .toRoutes(createUserEndpoint :: Nil)

    Http4sUserApiEndpoints(publicRoutes)
  }

}

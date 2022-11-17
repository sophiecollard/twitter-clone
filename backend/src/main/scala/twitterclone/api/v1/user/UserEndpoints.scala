package twitterclone.api.v1.user

import cats.effect.kernel.Concurrent
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.dsl.Http4sDsl
import twitterclone.api.syntax.{RequestSyntax, withNoServiceError}
import twitterclone.services.user.UserService

final case class UserEndpoints[F[_]](httpRoutes: HttpRoutes[F])

object UserEndpoints {

  def create[F[_]: Concurrent](
    service: UserService[F]
  ): UserEndpoints[F] = {
    object dsl extends Http4sDsl[F]
    import dsl._

    val publicRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
      case request @ POST -> Root =>
        request.withBodyAs[CreateUserRequest] { requestBody =>
          service.create(requestBody.handle, requestBody.name).flatMap {
            withNoServiceError { _ =>
              Created()
            }
          }
        }
    }

    UserEndpoints(publicRoutes)
  }

}

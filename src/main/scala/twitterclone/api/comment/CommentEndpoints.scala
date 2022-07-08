package twitterclone.api.comment

import cats.effect.Concurrent
import cats.implicits._
import org.http4s.{AuthedRoutes, HttpRoutes}
import org.http4s.dsl.Http4sDsl
import org.http4s.server.AuthMiddleware
import twitterclone.model.{Id, User}
import twitterclone.services.comment.CommentService

import scala.annotation.nowarn // TODO Remove

final case class CommentEndpoints[F[_]](httpRoutes: HttpRoutes[F])

object CommentEndpoints {

  @nowarn // TODO Remove
  def create[F[_]: Concurrent](
    authMiddleware: AuthMiddleware[F, Id[User]],
    service: CommentService[F]
  ): CommentEndpoints[F] = {
    object dsl extends Http4sDsl[F]
    import dsl._

    val publicRoutes: HttpRoutes[F] =
      ???

    val privateRoutes: AuthedRoutes[Id[User], F] =
      ???

    CommentEndpoints(publicRoutes <+> authMiddleware(privateRoutes))
  }

}

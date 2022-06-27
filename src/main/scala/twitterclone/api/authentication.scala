package twitterclone.api

import cats.data.{Kleisli, OptionT}
import cats.effect.IO
import org.http4s.server.AuthMiddleware
import twitterclone.model.{Id, User}

import java.util.UUID
import scala.util.Try

object authentication {

  /**
   * Dummy implementation of org.http4s.server.AuthMiddleware.
   *
   * This simply takes the value (which must be a UUID) of the "x-user-id" HTTP header in the request and uses it as the
   * current user ID. In other words, it just trusts that you are who you say you are. It goes without saying that this
   * is implemented for testing purposes only and that you should NEVER USE THIS IN PRODUCTION!
   */
  val dummyAuthMiddleware: AuthMiddleware[IO, Id[User]] = AuthMiddleware(
    Kleisli { request =>
      OptionT.fromOption[IO](
        request
          .headers.headers
          .find(_.name.toString == "x-user-id")
          .flatMap { rawHeader =>
            Try(UUID.fromString(rawHeader.value))
              .map(Id.apply[User])
              .toOption
          }
      )
    }
  )

}

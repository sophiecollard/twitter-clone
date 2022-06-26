package twitterclone.api

import cats.{Applicative, Monad}
import cats.implicits._
import org.http4s.dsl.io.Unauthorized
import org.http4s.{EntityDecoder, Request, Response}
import twitterclone.api.error.ApiError
import twitterclone.auth
import twitterclone.services.error.ServiceErrorOr

object syntax {

  implicit class RequestSyntax[F[_]](val request: Request[F]) extends AnyVal {
    def withBodyAs[B](
      ifNoDecodeFailure: B => F[Response[F]]
    )(
      implicit F: Monad[F],
      decoder: EntityDecoder[F, B]
    ): F[Response[F]] =
      request
        .attemptAs[B]
        .leftMap(_.toHttpResponse[F](request.httpVersion))
        .semiflatMap(ifNoDecodeFailure)
        .merge
  }

  def withSuccessfulAuthorization[F[_]: Applicative, R, Tag](
    ifSuccessful: R => F[Response[F]]
  )(
    authorizationResult: auth.WithAuthorization[R, Tag]
  ): F[Response[F]] =
    authorizationResult match {
      case auth.Success(result) => ifSuccessful(result)
      case auth.Failure(_)      => Response[F](Unauthorized).pure[F]
    }

  def withNoServiceError[F[_]: Applicative, R](
    ifNoError: R => F[Response[F]]
  )(
    serviceErrorOr: ServiceErrorOr[R]
  ): F[Response[F]] =
    serviceErrorOr match {
      case Right(result)      => ifNoError(result)
      case Left(serviceError) => ApiError.fromServiceError(serviceError).response
    }

}

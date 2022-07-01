package twitterclone

import org.scalatest.Assertion
import twitterclone.auth.error.AuthorizationError
import twitterclone.services.error.ServiceErrorOr

object testsyntax {

  def withSuccessfulAuthorization[R, Tag](
    authorizationResult: auth.WithAuthorization[R, Tag]
  )(
    ifSuccessful: R => Assertion
  ): Assertion =
    authorizationResult match {
      case auth.Success(result) => ifSuccessful(result)
      case auth.Failure(error)  => throw new AssertionError(s"Authorization failed: $error")
    }

  def withFailedAuthorization[R, Tag](
    authorizationResult: auth.WithAuthorization[R, Tag]
  )(
    ifFailed: AuthorizationError => Assertion
  ): Assertion =
    authorizationResult match {
      case auth.Failure(error) => ifFailed(error)
      case auth.Success(_)     => throw new AssertionError("Authorization succeeded")
    }

  def withNoServiceError[R](
    serviceErrorOr: ServiceErrorOr[R]
  )(
    ifNoError: R => Assertion
  ): Assertion =
    serviceErrorOr match {
      case Right(result) => ifNoError(result)
      case Left(error)   => throw new AssertionError(s"Got service error: $error")
    }

}

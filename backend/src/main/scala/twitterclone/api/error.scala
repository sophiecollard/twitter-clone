package twitterclone.api

import cats.Applicative
import cats.implicits._
import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json}
import org.http4s.Response
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.Http4sDsl
import twitterclone.services.error.ServiceError

object error {

  sealed abstract class ApiError(val responseBody: ErrorResponseBody) {
    final def response[F[_]: Applicative]: F[Response[F]] = {
      object dsl extends Http4sDsl[F]
      import dsl._

      this match {
        case error @ ApiError.FailedToCreateResource(_) =>
          InternalServerError(error.responseBody)
        case error @ ApiError.FailedToDeleteResource(_) =>
          InternalServerError(error.responseBody)
        case error @ ApiError.ResourceAlreadyExists(_) =>
          Conflict(error.responseBody)
        case error @ ApiError.ResourceCantBeDeleted(_) =>
          Conflict(error.responseBody)
        case error @ ApiError.ResourceNotFound(_) =>
          NotFound(error.responseBody)
        case error @ ApiError.UnexpectedError =>
          InternalServerError(error.responseBody)
      }
    }
  }

  object ApiError {

    final case class FailedToCreateResource(resourceName: String) extends ApiError(
      responseBody = ErrorResponseBody(
        code = Code("failed_to_create_resource"),
        message = Message(s"Unexpected failure while creating $resourceName."),
        help = Help("Please try one more time. If the error persists, this is probably a bug.").some
      )
    )

    final case class FailedToDeleteResource(resourceName: String) extends ApiError(
      responseBody = ErrorResponseBody(
        code = Code("failed_to_delete_resource"),
        message = Message(s"Unexpected failure while deleting $resourceName."),
        help = Help("Please try one more time. If the error persists, this is probably a bug.").some
      )
    )

    final case class ResourceAlreadyExists(message: String) extends ApiError(
      responseBody = ErrorResponseBody(
        code = Code("resource_already_exists"),
        message = Message(message)
      )
    )

    final case class ResourceCantBeDeleted(message: String) extends ApiError(
      responseBody = ErrorResponseBody(
        code = Code("resource_cant_be_deleted"),
        message = Message(message)
      )
    )

    final case class ResourceNotFound(message: String) extends ApiError(
      responseBody = ErrorResponseBody(
        code = Code("resource_not_found"),
        message = Message(message),
        help = Help("Please check that the resource identifier is correct.").some
      )
    )

    case object UnexpectedError extends ApiError(
      responseBody = ErrorResponseBody(
        code = Code("unexpected_error"),
        message = Message("An unexpected server error occurred.")
      )
    )

    def fromServiceError(serviceError: ServiceError): ApiError =
      serviceError match {
        case ServiceError.FailedToCreateResource(resourceType) =>
          FailedToCreateResource(resourceType)
        case ServiceError.FailedToDeleteResource(_, resourceType) =>
          FailedToDeleteResource(resourceType)
        case error @ ServiceError.ResourceNotFound(_, _) =>
          ResourceNotFound(error.message)
        case error @ ServiceError.ResourcesNotFound(_, _) =>
          ResourceNotFound(error.message)
        case error @ ServiceError.UserHandleAlreadyExists(_) =>
          ResourceAlreadyExists(error.message)
        case error @ ServiceError.UserHandleNotFound(_) =>
          ResourceNotFound(error.message)
      }

    implicit val encoder: Encoder[ApiError] =
      Encoder.instance { apiError =>
        apiError.responseBody.asJson
      }

    implicit val decoder: Decoder[ApiError] =
      Decoder[ErrorResponseBody].map { _ =>
        UnexpectedError // FIXME Map to the proper API error
      }

  }

  final case class ErrorResponseBody(
    code: Code,
    message: Message,
    help: Option[Help] = None
  )

  object ErrorResponseBody {
    implicit val encoder: Encoder[ErrorResponseBody] =
      Encoder.instance { error =>
        Json.obj(
          "error" := error.code.value.asJson,
          "message" := error.message.value.asJson,
          "help" := error.help.map(_.value).asJson
        )
      }

    implicit val decoder: Decoder[ErrorResponseBody] =
      Decoder.instance { cursor =>
        for {
          error <- cursor.downField("error").as[String].map(Code.apply)
          message <- cursor.downField("message").as[String].map(Message.apply)
          help <- cursor.downField("help").as[Option[String]].map(_.map(Help.apply))
        } yield ErrorResponseBody(error, message, help)
      }
  }

  final case class Code(value: String) extends AnyVal

  final case class Message(value: String) extends AnyVal

  final case class Help(value: String) extends AnyVal

}

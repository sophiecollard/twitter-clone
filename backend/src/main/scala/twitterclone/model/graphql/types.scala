package twitterclone.model.graphql

import sangria.schema.ScalarType
import sangria.validation.ValueCoercionViolation

import java.time.LocalDateTime
import java.util.UUID
import scala.util.{Failure, Success, Try}

object types {

  val UUIDType: ScalarType[UUID] = ScalarType[UUID](
    name = "UUID",
    description = Some("UUID"),
    coerceOutput = { case (value, _) => value },
    coerceUserInput = {
      case s: String =>
        Try(UUID.fromString(s)) match {
          case Success(uuid) => Right(uuid)
          case Failure(_) => Left(UUIDCoercionViolation)
        }
      case _ =>
        Left(UUIDCoercionViolation)
    },
    coerceInput = {
      case sangria.ast.StringValue(s, _, _, _, _) =>
        Try(UUID.fromString(s)) match {
          case Success(uuid) => Right(uuid)
          case Failure(_) => Left(UUIDCoercionViolation)
        }
      case _ =>
        Left(UUIDCoercionViolation)
    }
  )

  val LocalDateTimeType: ScalarType[LocalDateTime] = ScalarType[LocalDateTime](
    name = "LocalDateTime",
    description = Some("Local date and time information, must conform to the yyyy-mm-ddTHH:MM:SS pattern"),
    coerceOutput = { case (value, _) => value },
    coerceUserInput = {
      case s: String =>
        Try(LocalDateTime.parse(s)) match {
          case Success(localDateTime) => Right(localDateTime)
          case Failure(_) => Left(LocalDateTimeCoercionViolation)
        }
      case _ =>
        Left(LocalDateTimeCoercionViolation)
    },
    coerceInput = {
      case sangria.ast.StringValue(s, _, _, _, _) =>
        Try(LocalDateTime.parse(s)) match {
          case Success(localDateTime) => Right(localDateTime)
          case Failure(_) => Left(LocalDateTimeCoercionViolation)
        }
      case _ =>
        Left(LocalDateTimeCoercionViolation)
    }
  )

  case object UUIDCoercionViolation extends ValueCoercionViolation("UUID value expected")
  case object LocalDateTimeCoercionViolation extends ValueCoercionViolation("LocalDateTime value expected")

}

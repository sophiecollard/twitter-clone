package twitterclone.api.shared

import io.circe.{Decoder, Encoder}
import org.http4s.QueryParamDecoder
import twitterclone.model.Id

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

object instances {

  implicit def idEncoder[A]: Encoder[Id[A]] =
    Encoder[UUID].contramap(_.value)

  implicit def idDecoder[A]: Decoder[Id[A]] =
    Decoder[UUID].map(Id.apply[A])

  implicit def idQueryParamDecoder[A]: QueryParamDecoder[Id[A]] =
    QueryParamDecoder[UUID].map(Id.apply[A])

  implicit val uuidEncoder: Encoder[UUID] =
    Encoder[String].contramap(_.toString)

  implicit val uuidQueryParamDecoder: QueryParamDecoder[UUID] =
    QueryParamDecoder[String].map(UUID.fromString)

  implicit val localDateTimeEncoder: Encoder[LocalDateTime] =
    Encoder[String].contramap(_.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))

  implicit val localDateTimeQueryParamDecoder: QueryParamDecoder[LocalDateTime] =
    QueryParamDecoder[String].map(LocalDateTime.parse(_, DateTimeFormatter.ISO_LOCAL_DATE_TIME))

}

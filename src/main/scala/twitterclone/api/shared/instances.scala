package twitterclone.api.shared

import io.circe.Encoder
import org.http4s.QueryParamDecoder
import twitterclone.model.Id

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

object instances {

  implicit def idEncoder[A]: Encoder[Id[A]] =
    Encoder[UUID].contramap(_.value)

  implicit def idQueryParamDecoder[A]: QueryParamDecoder[Id[A]] =
    QueryParamDecoder[UUID].map(Id.apply[A])

  implicit val uuidEncoder: Encoder[UUID] =
    Encoder[String].contramap(_.toString)

  implicit val uuidQueryParamDecoder: QueryParamDecoder[UUID] =
    QueryParamDecoder[String].map(UUID.fromString)

  implicit val zonedDateTimeEncoder: Encoder[ZonedDateTime] =
    Encoder[String].contramap(_.format(DateTimeFormatter.ISO_ZONED_DATE_TIME))

  implicit val zonedDateTimeQueryParamDecoder: QueryParamDecoder[ZonedDateTime] =
    QueryParamDecoder[String].map(ZonedDateTime.parse(_, DateTimeFormatter.ISO_ZONED_DATE_TIME))

}

package twitterclone.api

import io.circe.Encoder
import twitterclone.model.Id

import java.time.format.DateTimeFormatter
import java.time.ZonedDateTime
import java.util.UUID

object instances {

  implicit def idEncoder[A]: Encoder[Id[A]] =
    Encoder[UUID].contramap(_.value)

  implicit val uuidEncoder: Encoder[UUID] =
    Encoder[String].contramap(_.toString)

  implicit val zonedDateTimeEncoder: Encoder[ZonedDateTime] =
    Encoder[String].contramap(_.format(DateTimeFormatter.ISO_ZONED_DATE_TIME))

}

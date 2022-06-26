package twitterclone.repositories.shared

import doobie.Meta
import twitterclone.model.Id

import java.time.format.DateTimeFormatter
import java.time.ZonedDateTime
import java.util.UUID

object instances {

  implicit val uuidMeta: Meta[UUID] =
    Meta[String].imap(UUID.fromString)(_.toString)

  implicit def idMeta[A]: Meta[Id[A]] =
    Meta[UUID].imap(Id.apply[A])(_.value)

  implicit val zonedDateTimeMeta: Meta[ZonedDateTime] = {
    val formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME
    Meta[String].imap(ZonedDateTime.parse(_, formatter))(_.format(formatter))
  }

}

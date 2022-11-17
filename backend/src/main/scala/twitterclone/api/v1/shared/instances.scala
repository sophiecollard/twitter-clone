package twitterclone.api.v1.shared

import org.http4s.QueryParamDecoder
import twitterclone.model.Id

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

object instances {

  implicit def idQueryParamDecoder[A]: QueryParamDecoder[Id[A]] =
    QueryParamDecoder[UUID].map(Id.apply[A])

  implicit val uuidQueryParamDecoder: QueryParamDecoder[UUID] =
    QueryParamDecoder[String].map(UUID.fromString)

  implicit val localDateTimeQueryParamDecoder: QueryParamDecoder[LocalDateTime] =
    QueryParamDecoder[String].map(LocalDateTime.parse(_, DateTimeFormatter.ISO_LOCAL_DATE_TIME))

}

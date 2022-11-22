package twitterclone.model

import io.circe.{Decoder, Encoder}

import java.util.UUID

final case class Id[A](value: UUID) extends AnyVal

object Id {

  def random[A]: Id[A] =
    Id(UUID.randomUUID())

  implicit def idEncoder[A]: Encoder[Id[A]] =
    Encoder.encodeUUID.contramap(_.value)

  implicit def idDecoder[A]: Decoder[Id[A]] =
    Decoder.decodeUUID.map(Id.apply[A])

}

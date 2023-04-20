package twitterclone.model

import io.circe.{Decoder, Encoder}
import sttp.tapir.{Codec, CodecFormat}

import java.util.UUID

final case class Id[A](value: UUID) extends AnyVal

object Id {

  implicit def codec[A]: Codec[String, Id[A], CodecFormat.TextPlain] =
    Codec.uuid.map(apply[A](_))(_.value)

  implicit def decoder[A]: Decoder[Id[A]] =
    Decoder.decodeUUID.map(apply[A])

  implicit def encoder[A]: Encoder[Id[A]] =
    Encoder.encodeUUID.contramap(_.value)

  def random[A]: Id[A] =
    Id(UUID.randomUUID())

}

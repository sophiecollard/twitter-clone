package twitterclone.model.user

import io.circe.{Decoder, Encoder}

sealed abstract case class Handle(value: String)

object Handle {

  private val pattern = "^[a-z0-9-_]{1,24}$".r

  def fromString(value: String): Either[String, Handle] =
    pattern findFirstIn value match {
      case Some(handleValue) =>
        Right(new Handle(handleValue) {})
      case None =>
        Left(s"$value does not match the following regex: ${pattern.toString}")
    }

  def unsafeFromString(value: String): Handle =
    fromString(value) match {
      case Right(handle) => handle
      case Left(error)   => throw new RuntimeException(error)
    }

  implicit val decoder: Decoder[Handle] =
    ???

  implicit val encoder: Encoder[Handle] =
    Encoder.encodeString.contramap(_.value)

}

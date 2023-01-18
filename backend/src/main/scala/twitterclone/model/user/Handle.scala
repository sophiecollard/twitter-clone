package twitterclone.model.user

import doobie.Meta
import eu.timepit.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.string._
import io.circe.{Decoder, Encoder}

object Handle {

  type Predicate = MatchesRegex[W.`"^[a-z0-9-_]{1,24}$"`.T]
  type Value = String Refined Predicate

  def apply(value: Value): Value =
    value

  def fromString(value: String): Either[String, Value] =
    refineV[Predicate](value)

  private def unsafeFromString(value: String): Value =
    fromString(value) match {
      case Right(handle) => handle
      case Left(error)   => throw new IllegalArgumentException(error)
    }

  implicit val meta: Meta[Value] =
    Meta.StringMeta.imap(unsafeFromString)(_.value)

  implicit val encoder: Encoder[Value] =
    Encoder.encodeString.contramap(_.value)

  implicit val decoder: Decoder[Value] =
   Decoder.decodeString.emap(fromString)

}

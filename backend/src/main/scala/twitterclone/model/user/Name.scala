package twitterclone.model.user

import eu.timepit.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.string._
import io.circe.{Decoder, Encoder}

sealed abstract case class Name(value: String)

object Name {

  type Predicate = MatchesRegex[W.`"^[a-zA-Z0-9-_ ]{1,36}$"`.T]
  type Value = String Refined Predicate

  def fromString(value: String): Either[String, Value] =
    refineV[Predicate](value)

  def unsafeFromString(value: String): Value =
    fromString(value) match {
      case Right(name) => name
      case Left(error) => throw new IllegalArgumentException(error)
    }

  implicit val encoder: Encoder[Value] =
    Encoder.encodeString.contramap(_.value)

  implicit val decoder: Decoder[Value] =
    Decoder.decodeString.emap(fromString)

}

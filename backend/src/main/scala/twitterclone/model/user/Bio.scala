package twitterclone.model.user

import io.circe.{Decoder, Encoder}

final case class Bio(value: String) extends AnyVal

object Bio {

  implicit val decoder: Decoder[Bio] =
    ???

  implicit val encoder: Encoder[Bio] =
    ???

}

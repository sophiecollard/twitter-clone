package twitterclone.model

import io.circe.{Decoder, Encoder}

sealed trait TweetReaction extends Product with Serializable

object TweetReaction {

  final case object Liked extends TweetReaction

  final case object NoReaction extends TweetReaction

  implicit val decoder: Decoder[TweetReaction] =
    Decoder.decodeString.emap {
      case "Liked"      => Right(Liked)
      case "NoReaction" => Right(NoReaction)
      case other        => Left(s"Not a valid tweet reaction: $other")
    }

  implicit val encoder: Encoder[TweetReaction] =
    Encoder.encodeString.contramap(_.toString)

}

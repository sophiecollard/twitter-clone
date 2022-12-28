package twitterclone.model

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import twitterclone.model.user.User

import java.time.LocalDateTime

final case class Tweet(
  id: Id[Tweet],
  authorId: Id[User],
  contents: String,
  postedOn: LocalDateTime
)

object Tweet {

  implicit val encoder: Encoder[Tweet] =
    deriveEncoder

  implicit val decoder: Decoder[Tweet] =
    deriveDecoder

}

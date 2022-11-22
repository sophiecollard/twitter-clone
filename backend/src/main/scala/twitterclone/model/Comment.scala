package twitterclone.model

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import twitterclone.model.user.User

import java.time.LocalDateTime

final case class Comment(
  id: Id[Comment],
  author: Id[User],
  tweetId: Id[Tweet],
  contents: String,
  postedOn: LocalDateTime
)

object Comment {

  implicit val encoder: Encoder[Comment] =
    deriveEncoder

  implicit val decoder: Decoder[Comment] =
    deriveDecoder

}

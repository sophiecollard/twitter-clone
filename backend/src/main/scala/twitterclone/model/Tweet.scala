package twitterclone.model

import eu.timepit.refined.types.numeric.NonNegInt
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.refined._
import twitterclone.model.user.User

import java.time.LocalDateTime

final case class Tweet(
  id: Id[Tweet],
  authorId: Id[User],
  contents: String,
  postedOn: LocalDateTime,
  likeCount: NonNegInt,
  didUserLike: Option[Boolean]
)

object Tweet {

  implicit val encoder: Encoder[Tweet] =
    deriveEncoder

  implicit val decoder: Decoder[Tweet] =
    deriveDecoder

}

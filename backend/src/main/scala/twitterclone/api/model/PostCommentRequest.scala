package twitterclone.api.model

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto
import twitterclone.model.{Id, Tweet}

final case class PostCommentRequest(
  tweetId: Id[Tweet],
  contents: String
)

object PostCommentRequest {

  implicit val decoder: Decoder[PostCommentRequest] =
    semiauto.deriveDecoder

  implicit val encoder: Encoder[PostCommentRequest] =
    semiauto.deriveEncoder

}

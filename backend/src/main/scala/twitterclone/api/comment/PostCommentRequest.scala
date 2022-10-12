package twitterclone.api.comment

import io.circe.Decoder
import io.circe.generic.semiauto
import twitterclone.api.shared.instances.idDecoder
import twitterclone.model.{Id, Tweet}

final case class PostCommentRequest(
  tweetId: Id[Tweet],
  contents: String
)

object PostCommentRequest {
  implicit val decoder: Decoder[PostCommentRequest] =
    semiauto.deriveDecoder
}

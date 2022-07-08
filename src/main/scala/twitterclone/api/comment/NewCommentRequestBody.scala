package twitterclone.api.comment

import io.circe.Decoder
import io.circe.generic.semiauto
import twitterclone.api.shared.instances.idDecoder
import twitterclone.model.{Id, Tweet}

final case class NewCommentRequestBody(
  tweetId: Id[Tweet],
  contents: String
)

object NewCommentRequestBody {
  implicit val decoder: Decoder[NewCommentRequestBody] =
    semiauto.deriveDecoder
}

package twitterclone.api.tweet

import io.circe.Decoder

final case class NewTweetRequestBody(contents: String)

object NewTweetRequestBody {
  implicit val decoder: Decoder[NewTweetRequestBody] =
    Decoder[String].map(apply)
}

package twitterclone.api.tweet

import io.circe.Decoder

final case class NewTweetRequestBody(contents: String)

object NewTweetRequestBody {
  implicit val decoder: Decoder[NewTweetRequestBody] =
    Decoder.instance { hCursor =>
      hCursor
        .downField("contents")
        .as[String]
        .map(apply)
    }
}

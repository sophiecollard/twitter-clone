package twitterclone.api.v1.tweet

import io.circe.Decoder

final case class PostTweetRequest(contents: String)

object PostTweetRequest {
  implicit val decoder: Decoder[PostTweetRequest] =
    Decoder.instance { hCursor =>
      hCursor
        .downField("contents")
        .as[String]
        .map(apply)
    }
}

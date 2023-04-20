package twitterclone.api.model

import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax._

final case class PostTweetRequest(contents: String)

object PostTweetRequest {

  implicit val decoder: Decoder[PostTweetRequest] =
    Decoder.instance { hCursor =>
      hCursor
        .downField("contents")
        .as[String]
        .map(apply)
    }

  implicit val encoder: Encoder[PostTweetRequest] =
    Encoder.instance { req =>
      Json.obj(fields = "contents" := req.contents)
    }

}

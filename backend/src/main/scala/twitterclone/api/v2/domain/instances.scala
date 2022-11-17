package twitterclone.api.v2.domain

import sttp.tapir.{Codec, CodecFormat}
import twitterclone.model.{Comment, Id, Tweet}

object instances {

  implicit val commentIdCodec: Codec[String, Id[Comment], CodecFormat.TextPlain] =
    Codec.uuid.map(Id.apply[Comment](_))(_.value)

  implicit val tweetIdCodec: Codec[String, Id[Tweet], CodecFormat.TextPlain] =
    Codec.uuid.map(Id.apply[Tweet](_))(_.value)

}

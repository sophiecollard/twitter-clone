package twitterclone.api.tweet

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import twitterclone.api.instances._
import twitterclone.model.Tweet

object instances {

  implicit val tweetEncoder: Encoder[Tweet] =
    deriveEncoder[Tweet]

}

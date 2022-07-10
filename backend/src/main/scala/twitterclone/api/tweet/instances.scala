package twitterclone.api.tweet

import io.circe.Encoder
import io.circe.generic.semiauto
import twitterclone.api.shared.instances._
import twitterclone.model.Tweet

object instances {

  implicit val tweetEncoder: Encoder[Tweet] =
    semiauto.deriveEncoder

}

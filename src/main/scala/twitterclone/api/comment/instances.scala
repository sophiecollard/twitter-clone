package twitterclone.api.comment

import io.circe.Encoder
import io.circe.generic.semiauto
import twitterclone.api.shared.instances._
import twitterclone.model.Comment

object instances {

  implicit val commentEncoder: Encoder[Comment] =
    semiauto.deriveEncoder

}

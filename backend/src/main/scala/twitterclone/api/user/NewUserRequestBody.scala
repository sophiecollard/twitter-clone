package twitterclone.api.user

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import twitterclone.model.user.{Handle, Name}

final case class NewUserRequestBody(
  handle: Handle,
  name: Name
)

object NewUserRequestBody {

  implicit val decoder: Decoder[NewUserRequestBody] =
    deriveDecoder

}

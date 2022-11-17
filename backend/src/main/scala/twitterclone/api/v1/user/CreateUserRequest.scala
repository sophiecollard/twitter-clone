package twitterclone.api.v1.user

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import twitterclone.model.user.{Handle, Name}

final case class CreateUserRequest(
  handle: Handle,
  name: Name
)

object CreateUserRequest {
  implicit val decoder: Decoder[CreateUserRequest] =
    deriveDecoder
}

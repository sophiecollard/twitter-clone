package twitterclone.api.user

import io.circe.{Decoder, Encoder}
import twitterclone.model.user.{Bio, Handle, Name}

final case class CreateUserRequest(
  handle: Handle,
  name: Name,
  bio: Bio
)

object CreateUserRequest {

  implicit val decoder: Decoder[CreateUserRequest] =
    ???

  implicit val encoder: Encoder[CreateUserRequest] =
    ???

}

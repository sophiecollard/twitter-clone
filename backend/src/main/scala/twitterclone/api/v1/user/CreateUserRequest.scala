package twitterclone.api.v1.user

import io.circe.Decoder
import twitterclone.model.user.{Handle, Name}

final case class CreateUserRequest(
  handle: Handle.Value,
  name: Name.Value
)

object CreateUserRequest {

  implicit val decoder: Decoder[CreateUserRequest] =
    Decoder.instance { cursor =>
      for {
        handle <- cursor.get[Handle.Value]("handle")(Handle.decoder)
        name <- cursor.get[Name.Value]("name")(Name.decoder)
      } yield CreateUserRequest(handle, name)
    }

}

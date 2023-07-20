package twitterclone.api.model

import io.circe.{Decoder, Encoder, Json}
import io.circe.refined._
import io.circe.syntax._
import twitterclone.model.user.{Handle, Name}

final case class UserSignUpRequest(handle: Handle.Value, name: Name.Value)

object UserSignUpRequest {

  implicit val decoder: Decoder[UserSignUpRequest] =
    Decoder.instance { cursor =>
      for {
        handle <- cursor.get[Handle.Value]("handle")
        name <- cursor.get[Name.Value]("name")
      } yield UserSignUpRequest(handle, name)
    }

  implicit val encoder: Encoder[UserSignUpRequest] =
    Encoder.instance[UserSignUpRequest] { req =>
      Json.obj(fields =
        "handle" := req.handle,
        "name" := req.name
      )
    }

}

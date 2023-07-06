package twitterclone.model.user

import io.circe.{Decoder, Encoder, Json}
import io.circe.refined._
import io.circe.syntax._
import twitterclone.model.Id

import java.time.LocalDateTime

final case class User(
  id: Id[User],
  handle: Handle.Value,
  name: Name.Value,
  status: Status,
  registeredOn: LocalDateTime
)

object User {

  implicit val decoder: Decoder[User] =
    Decoder.instance { cursor =>
      for {
        id <- cursor.get[Id[User]]("id")
        handle <- cursor.get[Handle.Value]("handle")
        name <- cursor.get[Name.Value]("name")
        status <- cursor.get[Status]("status")
        registeredOn <- cursor.get[LocalDateTime]("registeredOn")
      } yield User(id, handle, name, status, registeredOn)
    }

  implicit val encoder: Encoder[User] =
    Encoder.instance { user =>
      Json.obj(fields =
        "id" := user.id,
        "handle" := user.handle,
        "name" := user.name,
        "status" := user.status,
        "registeredOn" := user.registeredOn
      )
    }

}

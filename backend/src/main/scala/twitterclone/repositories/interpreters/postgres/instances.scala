package twitterclone.repositories.interpreters.postgres

import doobie.Meta
import twitterclone.model.Id

import java.util.UUID

object instances {

  implicit val uuidMeta: Meta[UUID] =
    Meta[String].imap(UUID.fromString)(_.toString)

  implicit def idMeta[A]: Meta[Id[A]] =
    Meta[UUID].imap(Id.apply[A])(_.value)

}

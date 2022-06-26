package twitterclone.model

import java.util.UUID

final case class Id[A](value: UUID)

object Id {
  def random[A]: Id[A] =
    Id(UUID.randomUUID())
}

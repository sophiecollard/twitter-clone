package twitterclone.api.shared

import twitterclone.model.{Id, Tweet}

import java.util.UUID
import scala.util.Try

object extractors {

  object TweetIdVar {
    def unapply(value: String): Option[Id[Tweet]] =
      Try(UUID.fromString(value))
        .map(Id.apply[Tweet])
        .toOption
  }

}

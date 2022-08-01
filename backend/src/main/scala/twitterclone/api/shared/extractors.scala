package twitterclone.api.shared

import twitterclone.model.user.User
import twitterclone.model.{Comment, Id, Tweet}

import java.util.UUID
import scala.util.Try

object extractors {

  object CommentIdVar {
    def unapply(value: String): Option[Id[Comment]] =
      Try(UUID.fromString(value))
        .map(Id.apply[Comment])
        .toOption
  }

  object TweetIdVar {
    def unapply(value: String): Option[Id[Tweet]] =
      Try(UUID.fromString(value))
        .map(Id.apply[Tweet])
        .toOption
  }

  object UserIdVar {
    def unapply(value: String): Option[Id[User]] =
      Try(UUID.fromString(value))
        .map(Id.apply[User])
        .toOption
  }

}

package twitterclone.auth

import twitterclone.model.user.User
import twitterclone.model.{Id, Comment, Tweet}

object error {

  sealed abstract class AuthorizationError(val message: String)

  type AuthorizationErrorOr[A] = Either[AuthorizationError, A]

  object AuthorizationError {

    final case class NotTheCommentsAuthor(userId: Id[User], commentId: Id[Comment])
      extends AuthorizationError(
        message = s"User [${userId.value}] is not the author of Comment [${commentId.value}]"
      )

    final case class NotTheTweetsAuthor(userId: Id[User], tweetId: Id[Tweet])
      extends AuthorizationError(
        message = s"User [${userId.value}] is not the author of Tweet [${tweetId.value}]"
      )

  }

}

package twitterclone.services.comment

import cats.Monad
import cats.implicits._
import twitterclone.auth.error.AuthorizationError.NotTheCommentsAuthor
import twitterclone.auth.{AuthorizationService, WithAuthorization}
import twitterclone.model.user.User
import twitterclone.model.{Id, Comment}
import twitterclone.repositories.comment.CommentRepository

object auth {

  trait ByAuthor
  type WithAuthorizationByAuthor[R] = WithAuthorization[R, ByAuthor]

  def byAuthor[F[_]: Monad](
    commentRepository: CommentRepository[F]
  ): AuthorizationService[F, (Id[User], Id[Comment]), ByAuthor] =
    AuthorizationService.create { case (userId, commentId) =>
      commentRepository.getAuthor(commentId).map {
        case Some(author) if author == userId =>
          Right(())
        case _ =>
          Left(NotTheCommentsAuthor(userId, commentId))
      }
    }

}

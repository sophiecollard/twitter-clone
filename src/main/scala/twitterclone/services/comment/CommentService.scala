package twitterclone.services.comment

import cats.{Monad, ~>}
import twitterclone.auth.AuthorizationService
import twitterclone.model.{Comment, CommentPagination, Id, Tweet, User}
import twitterclone.repositories.comment.CommentRepository
import twitterclone.services.comment.auth.{ByAuthor, WithAuthorizationByAuthor}
import twitterclone.services.error.ServiceErrorOr

import scala.annotation.nowarn

trait CommentService[F[_]] {

  /** Creates a new Comment */
  def create(tweetId: Id[Tweet], contents: String)(userId: Id[User]): F[ServiceErrorOr[Comment]]

  /** Deletes a Comment */
  def delete(id: Id[Comment])(userId: Id[User]): F[WithAuthorizationByAuthor[ServiceErrorOr[Unit]]]

  /** Fetches a Comment */
  def get(id: Id[Comment]): F[ServiceErrorOr[Comment]]

  /** Fetches comments for a given Tweet */
  def list(tweetId: Id[Tweet], pagination: CommentPagination = CommentPagination.default): F[ServiceErrorOr[List[Comment]]]

}

object CommentService {

  @nowarn // TODO Remove annotation once you begin implementation
  def create[F[_], G[_]: Monad](
    commentRepository: CommentRepository[G],
    authByAuthorService: AuthorizationService[G, (Id[User], Id[Comment]), ByAuthor]
  )(implicit transactor: G ~> F): CommentService[F] =
    new CommentService[F] {
      /** Creates a new Comment */
      override def create(tweetId: Id[Tweet], contents: String)(userId: Id[User]): F[ServiceErrorOr[Comment]] =
        ???

      /** Deletes a Comment */
      override def delete(id: Id[Comment])(userId: Id[User]): F[WithAuthorizationByAuthor[ServiceErrorOr[Unit]]] =
        ???

      /** Fetches a Comment */
      override def get(id: Id[Comment]): F[ServiceErrorOr[Comment]] =
        ???

      /** Fetches comments for a given Tweet */
      override def list(tweetId: Id[Tweet], pagination: CommentPagination = CommentPagination.default): F[ServiceErrorOr[List[Comment]]] =
        ???
    }

}

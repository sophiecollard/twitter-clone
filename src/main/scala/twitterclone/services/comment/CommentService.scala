package twitterclone.services.comment

import cats.implicits.{catsSyntaxEitherId, toFunctorOps}
import cats.{Monad, ~>}
import twitterclone.auth.AuthorizationService
import twitterclone.model.{Comment, CommentPagination, Id, Tweet, User}
import twitterclone.repositories.comment.CommentRepository
import twitterclone.services.comment.auth.{ByAuthor, WithAuthorizationByAuthor}
import twitterclone.services.error.ServiceError.{failedToCreateResource, failedToDeleteResource, resourceNotFound}
import twitterclone.services.error.{ServiceError, ServiceErrorOr}
import twitterclone.services.syntax.Transactable

import java.time.{LocalDateTime, ZoneId}

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

  def create[F[_], G[_]: Monad](
    commentRepository: CommentRepository[G],
    authByAuthorService: AuthorizationService[G, (Id[User], Id[Comment]), ByAuthor]
  )(implicit transactor: G ~> F): CommentService[F] =
    new CommentService[F] {
      /** Creates a new Comment */
      override def create(tweetId: Id[Tweet], contents: String)(userId: Id[User]): F[ServiceErrorOr[Comment]] = {
        val comment = Comment (
          id = Id.random[Comment],
          author = userId,
          tweetId,
          contents,
          postedOn = LocalDateTime.now(ZoneId.of("UTC"))
        )
        commentRepository.create(comment).map {
          case 1 => Right(comment)
          case _ => Left(failedToCreateResource("Comment"))
        }.transact
      }

      /** Deletes a Comment */
      override def delete(id: Id[Comment])(userId: Id[User]): F[WithAuthorizationByAuthor[ServiceErrorOr[Unit]]] =
        authByAuthorService.authorize((userId, id)) {
          commentRepository.delete(id).map {
            case 1 => Right(())
            case _ => Left(failedToDeleteResource(id, "Comment"))
          }
        }.transact

      /** Fetches a Comment */
      override def get(id: Id[Comment]): F[ServiceErrorOr[Comment]] =
        commentRepository.get(id).map {
          case Some(comment) => Right(comment)
          case None => Left(resourceNotFound(id, "Comment"))
        }.transact


      /** Fetches comments for a given Tweet */
      override def list(tweetId: Id[Tweet], pagination: CommentPagination = CommentPagination.default): F[ServiceErrorOr[List[Comment]]] =
        commentRepository
          .list(tweetId, pagination)
          .map(_.asRight[ServiceError])
          .transact
    }

}

package twitterclone.services.comment

import twitterclone.model.{Comment, CommentPagination, Id, Tweet, User}
import twitterclone.services.comment.auth.WithAuthorizationByAuthor
import twitterclone.services.error.ServiceErrorOr

trait CommentService[F[_]] {

  /** Creates a new Comment */
  def create(tweetId: Id[Tweet], contents: String)(userId: Id[User]): F[ServiceErrorOr[Comment]]

  /** Deletes a Comment */
  def delete(id: Id[Comment])(userId: Id[User]): F[WithAuthorizationByAuthor[ServiceErrorOr[Unit]]]

  /** Fetches a Comment */
  def get(id: Id[Comment]): F[ServiceErrorOr[Comment]]

  /** Fetches comments for a given Tweet */
  def list(tweetId: Id[Tweet], pagination: CommentPagination): F[ServiceErrorOr[List[Comment]]]

}

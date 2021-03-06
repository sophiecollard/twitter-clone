package twitterclone.repositories.comment

import twitterclone.model._
import twitterclone.model.user.User

trait CommentRepository[F[_]] {

  def create(comment: Comment): F[Int]

  def delete(id: Id[Comment]): F[Int]

  def get(id: Id[Comment]): F[Option[Comment]]

  def getAuthor(id: Id[Comment]): F[Option[Id[User]]]

  def list(tweetId: Id[Tweet], pagination: CommentPagination): F[List[Comment]]

}

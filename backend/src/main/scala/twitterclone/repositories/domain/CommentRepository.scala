package twitterclone.repositories.domain

import cats.~>
import twitterclone.model.user.User
import twitterclone.model.{Comment, CommentPagination, Id, Tweet}
import twitterclone.services.syntax.Transactable

trait CommentRepository[F[_]] {

  def create(comment: Comment): F[Int]

  def delete(id: Id[Comment]): F[Int]

  def get(id: Id[Comment]): F[Option[Comment]]

  def getAuthor(id: Id[Comment]): F[Option[Id[User]]]

  def list(tweetId: Id[Tweet], pagination: CommentPagination): F[List[Comment]]

  def listBy(authorId: Id[User], pagination: CommentPagination): F[List[Comment]]

}

object CommentRepository {

  def mapF[F[_], G[_]](repo: CommentRepository[F])(implicit transactor: F ~> G): CommentRepository[G] =
    new CommentRepository[G] {
      override def create(comment: Comment): G[Int] =
        repo.create(comment).transact

      override def delete(id: Id[Comment]): G[Int] =
        repo.delete(id).transact

      override def get(id: Id[Comment]): G[Option[Comment]] =
        repo.get(id).transact

      override def getAuthor(id: Id[Comment]): G[Option[Id[User]]] =
        repo.getAuthor(id).transact

      override def list(tweetId: Id[Tweet], pagination: CommentPagination): G[List[Comment]] =
        repo.list(tweetId, pagination).transact

      override def listBy(authorId: Id[User], pagination: CommentPagination): G[List[Comment]] =
        repo.listBy(authorId, pagination).transact
    }

}

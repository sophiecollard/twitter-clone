package twitterclone.repositories.domain

import cats.~>
import twitterclone.model.user.User
import twitterclone.model.{Id, Tweet, Pagination}
import twitterclone.services.syntax.Transactable

trait TweetRepository[F[_]] {

  def create(tweet: Tweet): F[Int]

  def delete(id: Id[Tweet]): F[Int]

  def get(id: Id[Tweet]): F[Option[Tweet]]

  def getAuthorId(id: Id[Tweet]): F[Option[Id[User]]]

  def list(pagination: Pagination): F[List[Tweet]]

  def listBy(authorId: Id[User], pagination: Pagination): F[List[Tweet]]

}

object TweetRepository {

  def mapF[F[_], G[_]](repo: TweetRepository[F])(implicit transactor: F ~> G): TweetRepository[G] =
    new TweetRepository[G] {
      override def create(tweet: Tweet): G[Int] =
        repo.create(tweet).transact

      override def delete(id: Id[Tweet]): G[Int] =
        repo.delete(id).transact

      override def get(id: Id[Tweet]): G[Option[Tweet]] =
        repo.get(id).transact

      override def getAuthorId(id: Id[Tweet]): G[Option[Id[User]]] =
        repo.getAuthorId(id).transact

      override def list(pagination: Pagination): G[List[Tweet]] =
        repo.list(pagination).transact

      override def listBy(authorId: Id[User], pagination: Pagination): G[List[Tweet]] =
        repo.listBy(authorId, pagination).transact
    }

}

package twitterclone.repositories.domain

import cats.~>
import twitterclone.model.{Id, Pagination}
import twitterclone.model.user.Handle.Value
import twitterclone.model.user.{Handle, User}
import twitterclone.services.syntax.Transactable

trait UserRepository[F[_]] {

  def create(user: User): F[Int]

  def delete(id: Id[User]): F[Int]

  def get(id: Id[User]): F[Option[User]]

  def getByHandle(handle: Handle.Value): F[Option[User]]

  def exists(handle: Handle.Value): F[Boolean]

  def list(pagination: Pagination): F[List[User]]

}

object UserRepository {

  def mapF[F[_], G[_]](repo: UserRepository[F])(implicit transactor: F ~> G): UserRepository[G] =
    new UserRepository[G] {
      override def create(user: User): G[Int] =
        repo.create(user).transact

      override def delete(id: Id[User]): G[Int] =
        repo.delete(id).transact

      override def get(id: Id[User]): G[Option[User]] =
        repo.get(id).transact

      override def getByHandle(handle: Value): G[Option[User]] =
        repo.getByHandle(handle).transact

      override def exists(handle: Value): G[Boolean] =
        repo.exists(handle).transact

      override def list(pagination: Pagination): G[List[User]] =
        repo.list(pagination).transact
    }

}

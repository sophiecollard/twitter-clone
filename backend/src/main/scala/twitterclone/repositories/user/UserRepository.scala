package twitterclone.repositories.user

import twitterclone.model.Id
import twitterclone.model.user.{Handle, User}

trait UserRepository[F[_]] {

  def create(user: User): F[Int]

  def delete(id: Id[User]): F[Int]

  def get(id: Id[User]): F[Option[User]]

  def getMany(ids: List[Id[User]]): F[List[User]]

  def getByHandle(handle: Handle): F[Option[User]]

}

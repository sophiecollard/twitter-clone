package twitterclone.repositories.interpreters.local

import cats.Applicative
import cats.implicits._
import twitterclone.model.{Id, Pagination}
import twitterclone.model.user.{Handle, User}
import twitterclone.repositories.domain.UserRepository

import java.time.LocalDateTime
import scala.collection.concurrent.TrieMap

object LocalUserRepository {

  def create[F[_]: Applicative](state: TrieMap[Id[User], User] = TrieMap.empty): UserRepository[F] =
    new UserRepository[F] {
      override def create(user: User): F[Int] =
        if (state.contains(user.id))
          0.pure[F]
        else
          state.addOne((user.id, user)).pure[F].map(_ => 1)

      override def delete(id: Id[User]): F[Int] =
        if (state.contains(id))
          state.remove(id).pure[F].map(_ => 1)
        else
          0.pure[F]

      override def get(id: Id[User]): F[Option[User]] =
        state.get(id).pure[F]

      override def getByHandle(handle: Handle.Value): F[Option[User]] =
        state
          .values
          .toList
          .find(_.handle == handle)
          .pure[F]

      override def exists(handle: Handle.Value): F[Boolean] =
        state
          .values
          .exists(_.handle == handle)
          .pure[F]

      override def list(pagination: Pagination): F[List[User]] =
        state
          .values
          .filter(u => pagination.postedBefore.forall(u.registeredOn isBefore _))
          .toList
          .sortBy(_.registeredOn)(Ordering[LocalDateTime].reverse)
          .take(pagination.pageSize)
          .pure[F]
    }

}

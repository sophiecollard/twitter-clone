package twitterclone.services.user

import cats.implicits._
import cats.{Monad, ~>}
import twitterclone.model.Id
import twitterclone.model.user.{Handle, Name, Status, User}
import twitterclone.repositories.user.UserRepository
import twitterclone.services.error.ServiceError._
import twitterclone.services.error.ServiceErrorOr
import twitterclone.services.syntax._

trait UserService[F[_]] {

  /** Creates a new user */
  def create(handle: Handle, name: Name): F[ServiceErrorOr[User]]

  /** Fetches a user */
  def get(id: Id[User]): F[ServiceErrorOr[User]]

  /** Fetches a list of users */
  def getMany(ids: List[Id[User]]): F[ServiceErrorOr[List[User]]]

  /** Fetches a user by its handle */
  def getByHandle(handle: Handle): F[ServiceErrorOr[User]]

}

object UserService {

  def create[F[_], G[_]: Monad](
    userRepository: UserRepository[G]
  )(implicit transactor: G ~> F): UserService[F] =
    new UserService[F] {
      /** Creates a new user */
      override def create(handle: Handle, name: Name): F[ServiceErrorOr[User]] = {
        val user = User (
          id = Id.random[User],
          handle,
          name,
          status = Status.PendingActivation
        )

        userRepository.exists(handle).flatMap { handleAlreadyExists =>
          if (handleAlreadyExists)
            userHandleAlreadyExists(handle)
              .asLeft[User]
              .pure[G]
          else
            userRepository.create(user).map {
              case 1 => Right(user)
              case _ => Left(failedToCreateResource("User"))
            }
        }.transact
      }

      /** Fetches a user */
      override def get(id: Id[User]): F[ServiceErrorOr[User]] =
        userRepository.get(id).map {
          case Some(user) => Right(user)
          case None       => Left(resourceNotFound(id, "User"))
        }.transact

      /** Fetches a list of users */
      override def getMany(ids: List[Id[User]]): F[ServiceErrorOr[List[User]]] =
        userRepository
          .getMany(ids)
          .map { users =>
            val returnedUserIds = users.map(_.id)
            if (ids.forall(returnedUserIds contains _)) {
              Right(users)
            } else {
              val missingUserIds = ids.filterNot(returnedUserIds contains _)
              Left(resourcesNotFound(missingUserIds, "User"))
            }
          }
          .transact

      /** Fetches a user by its handle */
      override def getByHandle(handle: Handle): F[ServiceErrorOr[User]] =
        userRepository.getByHandle(handle).map {
          case Some(user) => Right(user)
          case None       => Left(userHandleNotFound(handle))
        }.transact
    }

}

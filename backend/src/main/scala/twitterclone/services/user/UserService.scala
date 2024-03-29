package twitterclone.services.user

import cats.implicits._
import cats.{Monad, ~>}
import twitterclone.model.Id
import twitterclone.model.user.{Handle, Name, Status, User}
import twitterclone.repositories.domain.UserRepository
import twitterclone.services.error.ServiceError._
import twitterclone.services.error.ServiceErrorOr
import twitterclone.services.syntax._

import java.time.{LocalDateTime, ZoneId}

trait UserService[F[_]] {

  /** Creates a new user */
  def create(handle: Handle.Value, name: Name.Value): F[ServiceErrorOr[User]]

  /** Fetches a user */
  def get(id: Id[User]): F[ServiceErrorOr[User]]

  /** Fetches a user by its handle */
  def getByHandle(handle: Handle.Value): F[ServiceErrorOr[User]]

}

object UserService {

  def create[F[_], G[_]: Monad](
    userRepository: UserRepository[G]
  )(implicit transactor: G ~> F): UserService[F] =
    new UserService[F] {
      /** Creates a new user */
      override def create(handle: Handle.Value, name: Name.Value): F[ServiceErrorOr[User]] = {
        val user = User(
          id = Id.random[User],
          handle,
          name,
          status = Status.PendingActivation,
          registeredOn = LocalDateTime.now(ZoneId.of("UTC"))
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

      /** Fetches a user by its handle */
      override def getByHandle(handle: Handle.Value): F[ServiceErrorOr[User]] =
        userRepository.getByHandle(handle).map {
          case Some(user) => Right(user)
          case None       => Left(userHandleNotFound(handle))
        }.transact
    }

}

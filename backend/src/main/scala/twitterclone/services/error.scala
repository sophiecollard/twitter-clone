package twitterclone.services

import twitterclone.model.Id

object error {

  sealed abstract class ServiceError(val message: String) extends Product

  type ServiceErrorOr[A] = Either[ServiceError, A]

  object ServiceError {

    final case class FailedToCreateResource(`type`: String)
      extends ServiceError(
        message = s"Failed to create new ${`type`}"
      )

    final case class FailedToDeleteResource[A](id: Id[A], `type`: String)
      extends ServiceError(
        message = s"Failed to delete ${`type`} with id [${id.value}]"
      )

    final case class ResourceNotFound[A](id: Id[A], `type`: String)
      extends ServiceError(
        message = s"${`type`} with id [${id.value}] not found"
      )

    def failedToCreateResource(`type`: String): ServiceError =
      FailedToCreateResource(`type`)

    def failedToDeleteResource[A](id: Id[A], `type`: String): ServiceError =
      FailedToDeleteResource(id, `type`)

    def resourceNotFound[A](id: Id[A], `type`: String): ServiceError =
      ResourceNotFound(id, `type`)

  }

}

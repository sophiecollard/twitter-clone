package twitterclone.services

import twitterclone.model.Id
import twitterclone.model.user.Handle

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

    final case class ResourcesNotFound[A](ids: List[Id[A]], `type`: String)
      extends ServiceError(
        message = s"""${`type`}s with ids [${ids.map(_.value).mkString(",")}] not found"""
      )

    final case class UserHandleAlreadyExists(handle: Handle.Value)
      extends ServiceError(
        message = s"User with handle [${handle.value}] already exists"
      )

    final case class UserHandleNotFound(handle: Handle.Value)
      extends ServiceError(
        message = s"User with handle [${handle.value}] not found"
      )

    final case class GraphQLInterpretationError(error: String)
      extends ServiceError(
        message = s"GraphQL interpretation error: $error"
      )

    def failedToCreateResource(`type`: String): ServiceError =
      FailedToCreateResource(`type`)

    def failedToDeleteResource[A](id: Id[A], `type`: String): ServiceError =
      FailedToDeleteResource(id, `type`)

    def resourceNotFound[A](id: Id[A], `type`: String): ServiceError =
      ResourceNotFound(id, `type`)

    def resourcesNotFound[A](ids: List[Id[A]], `type`: String): ServiceError =
      ResourcesNotFound(ids, `type`)

    def userHandleAlreadyExists(handle: Handle.Value): ServiceError =
      UserHandleAlreadyExists(handle)

    def userHandleNotFound(handle: Handle.Value): ServiceError =
      UserHandleNotFound(handle)

    def graphQLInterpretationError(error: String): ServiceError =
      GraphQLInterpretationError(error)

  }

}

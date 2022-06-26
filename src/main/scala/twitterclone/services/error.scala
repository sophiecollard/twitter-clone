package twitterclone.services

object error {

  sealed abstract class ServiceError(val message: String)

  type ServiceErrorOr[A] = Either[ServiceError, A]

}

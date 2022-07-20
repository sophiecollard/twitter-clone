package twitterclone.model.user

sealed trait Status

object Status {
  case object PendingActivation extends Status
  case object Active            extends Status
  case object Suspended         extends Status
}

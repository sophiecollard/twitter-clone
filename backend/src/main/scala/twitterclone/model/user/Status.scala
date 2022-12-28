package twitterclone.model.user

import enumeratum._

sealed trait Status extends EnumEntry

object Status extends Enum[Status] with CirceEnum[Status] with DoobieEnum[Status] {
  case object PendingActivation extends Status
  case object Active            extends Status
  case object Suspended         extends Status

  val values = findValues
}

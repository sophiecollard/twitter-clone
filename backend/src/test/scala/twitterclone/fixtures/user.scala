package twitterclone.fixtures

import eu.timepit.refined.auto._
import twitterclone.model.Id
import twitterclone.model.user.{Handle, Name, Status, User}

import java.time.LocalDateTime

object user {

  val pendingActivationUser: User =
    User(
      id = Id.random[User],
      handle = Handle("sophie"),
      name = Name("Sophie"),
      status = Status.PendingActivation,
      registeredOn = LocalDateTime.MIN
    )

  val activeUser: User =
    User(
      id = Id.random[User],
      handle = Handle("travis"),
      name = Name("Travis"),
      status = Status.Active,
      registeredOn = LocalDateTime.MIN
    )

  val suspendedUser: User =
    User(
      id = Id.random[User],
      handle = Handle("john"),
      name = Name("John"),
      status = Status.Suspended,
      registeredOn = LocalDateTime.MIN
    )

}

package twitterclone.fixtures

import eu.timepit.refined.auto._
import twitterclone.model.Id
import twitterclone.model.user.{Handle, Name, Status, User}

object user {

  val pendingActivationUser: User =
    User(
      id = Id.random[User],
      handle = Handle("sophie"),
      name = Name("Sophie"),
      status = Status.PendingActivation
    )

  val activeUser: User =
    User(
      id = Id.random[User],
      handle = Handle("travis"),
      name = Name("Travis"),
      status = Status.Active
    )

  val suspendedUser: User =
    User(
      id = Id.random[User],
      handle = Handle("john"),
      name = Name("John"),
      status = Status.Suspended
    )

}

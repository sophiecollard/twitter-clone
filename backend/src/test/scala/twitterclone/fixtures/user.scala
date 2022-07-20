package twitterclone.fixtures

import twitterclone.model.Id
import twitterclone.model.user.{Handle, Name, Status, User}

object user {

  val pendingActivationUser: User =
    User(
      id = Id.random[User],
      handle = Handle.unsafeFromString("sophie"),
      name = Name.unsafeFromString("Sophie"),
      status = Status.PendingActivation
    )

  val activeUser: User =
    User(
      id = Id.random[User],
      handle = Handle.unsafeFromString("travis"),
      name = Name.unsafeFromString("Travis"),
      status = Status.Active
    )

  val suspendedUser: User =
    User(
      id = Id.random[User],
      handle = Handle.unsafeFromString("john"),
      name = Name.unsafeFromString("John"),
      status = Status.Suspended
    )

}

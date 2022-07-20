package twitterclone.model.user

import twitterclone.model.Id

final case class User(id: Id[User], handle: Handle, name: Name, status: Status)

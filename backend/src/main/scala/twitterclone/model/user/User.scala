package twitterclone.model.user

import twitterclone.model.Id

final case class User(id: Id[User], handle: Handle.Value, name: Name.Value, status: Status)

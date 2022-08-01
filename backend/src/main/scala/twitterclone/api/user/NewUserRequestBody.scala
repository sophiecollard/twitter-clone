package twitterclone.api.user

import twitterclone.model.user.{Handle, Name}

final case class NewUserRequestBody(
  handle: Handle,
  name: Name
)

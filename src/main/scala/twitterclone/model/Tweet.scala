package twitterclone.model

import java.time.ZonedDateTime

final case class Tweet(
  id: Id[Tweet],
  author: Id[User],
  contents: String,
  postedOn: ZonedDateTime
)

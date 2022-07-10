package twitterclone.model

import java.time.LocalDateTime

final case class Tweet(
  id: Id[Tweet],
  author: Id[User],
  contents: String,
  postedOn: LocalDateTime
)

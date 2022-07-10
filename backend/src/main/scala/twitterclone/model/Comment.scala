package twitterclone.model

import java.time.LocalDateTime

final case class Comment(
  id: Id[Comment],
  author: Id[User],
  tweetId: Id[Tweet],
  contents: String,
  postedOn: LocalDateTime
)

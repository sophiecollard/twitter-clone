package twitterclone.model

import java.time.ZonedDateTime

final case class Comment(
  id: Id[Comment],
  author: Id[User],
  tweetId: Id[Tweet],
  contents: String,
  postedOn: ZonedDateTime
)

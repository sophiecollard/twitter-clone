package twitterclone.model

import twitterclone.model.user.User

import java.time.LocalDateTime

final case class Comment(
  id: Id[Comment],
  author: Id[User],
  tweetId: Id[Tweet],
  contents: String,
  postedOn: LocalDateTime
)

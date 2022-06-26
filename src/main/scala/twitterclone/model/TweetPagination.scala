package twitterclone.model

import java.time.ZonedDateTime

final case class TweetPagination(
  pageSize: Int,
  postedAfter: ZonedDateTime
)

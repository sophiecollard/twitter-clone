package twitterclone.model

import java.time.ZonedDateTime

final case class TweetPagination(
  pageSize: Int,
  postedAfter: Option[ZonedDateTime]
)

object TweetPagination {
  lazy val default: TweetPagination =
    TweetPagination(
      pageSize = 10,
      postedAfter = None
    )
}

package twitterclone.model

import java.time.LocalDateTime

final case class TweetPagination(
  pageSize: Int,
  postedAfter: Option[LocalDateTime]
)

object TweetPagination {
  lazy val default: TweetPagination =
    TweetPagination(
      pageSize = 10,
      postedAfter = None
    )
}

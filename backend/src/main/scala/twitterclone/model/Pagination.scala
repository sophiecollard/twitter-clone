package twitterclone.model

import java.time.LocalDateTime

final case class Pagination(
  pageSize: Int,
  postedBefore: Option[LocalDateTime]
)

object Pagination {
  lazy val default: Pagination =
    Pagination(
      pageSize = 10,
      postedBefore = None
    )
}

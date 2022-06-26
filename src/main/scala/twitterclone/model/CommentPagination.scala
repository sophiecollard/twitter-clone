package twitterclone.model

import java.time.LocalDateTime

final case class CommentPagination(
  pageSize: Int,
  postedAfter: Option[LocalDateTime]
)

object CommentPagination {
  def default: CommentPagination =
    CommentPagination(
      pageSize = 10,
      postedAfter = None
    )
}

package twitterclone.model

import java.time.LocalDateTime

final case class CommentPagination(
  pageSize: Int,
  postedBefore: Option[LocalDateTime]
)

object CommentPagination {
  def default: CommentPagination =
    CommentPagination(
      pageSize = 10,
      postedBefore = None
    )
}

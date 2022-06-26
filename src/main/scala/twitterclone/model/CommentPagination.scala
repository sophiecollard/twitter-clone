package twitterclone.model

import java.time.{LocalDate, LocalTime, ZoneId, ZonedDateTime}

final case class CommentPagination(
  pageSize: Int,
  postedAfter: ZonedDateTime
)

object CommentPagination {
  def default: CommentPagination =
    CommentPagination(
      pageSize = 10,
      postedAfter = ZonedDateTime.of(
        LocalDate.of(1970, 1, 1),
        LocalTime.of(0, 0),
        ZoneId.of("UTC")
      )
    )
}

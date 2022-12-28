package twitterclone.model.graphql

import sangria.schema.{Argument, IntType, OptionInputType}
import twitterclone.model.graphql.types.{LocalDateTimeType, UUIDType}

import java.time.LocalDateTime
import java.util.UUID

object arguments {

  val UUIDArg: Argument[UUID] = Argument("id", UUIDType, description = "UUID")

  val TweetIdArg: Argument[UUID] = Argument("tweetId", UUIDType, description = "Tweet ID (UUID)")

  val PageSizeArg: Argument[Option[Int]] =
    Argument(
      name = "pageSize",
      description = "Number of elements to return",
      argumentType = OptionInputType(IntType)
    )

  val PostedBeforeArg: Argument[Option[LocalDateTime]] =
    Argument(
      name = "postedBefore",
      description = "Timestamp, must respect yyyy-mm-ddTHH:MM:SS pattern",
      argumentType = OptionInputType(LocalDateTimeType)
    )

}

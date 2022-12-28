package twitterclone.model.graphql

import sangria.schema._
import twitterclone.model.Tweet
import twitterclone.model.graphql.types.{LocalDateTimeType, UUIDType}
import twitterclone.services.AllServices

object TweetType {

  def apply[F[_]]: ObjectType[AllServices[F], Tweet] = {
    ObjectType(
      name = "Tweet",
      fieldsFn = () => fields(
        Field(
          name = "id",
          fieldType = UUIDType,
          resolve = _.value.id.value
        ),
        Field(
          name = "author",
          fieldType = UUIDType,
          resolve = _.value.author.value
        ),
        Field(
          name = "contents",
          fieldType = StringType,
          resolve = _.value.contents
        ),
        Field(
          name = "postedOn",
          fieldType = LocalDateTimeType,
          resolve = _.value.postedOn
        )
      )
    )
  }

}

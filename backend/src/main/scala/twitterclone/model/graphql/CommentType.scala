package twitterclone.model.graphql

import sangria.schema._
import twitterclone.model.Comment
import twitterclone.model.graphql.types.{LocalDateTimeType, UUIDType}
import twitterclone.repositories.domain.AllRepositories

object CommentType {

  def apply[F[_]]: ObjectType[AllRepositories[F], Comment] =
    ObjectType(
      name = "Comment",
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
          name = "tweetId",
          fieldType = UUIDType,
          resolve = _.value.tweetId.value
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
        ),
        Field(
          name = "tweet",
          fieldType = OptionType(TweetType[F]),
          resolve = { context =>
            DeferredType.TweetById(context.value.tweetId)
          }
        )
      )
    )

}

package twitterclone.model.graphql

import sangria.schema._
import twitterclone.model.{Pagination, Tweet}
import twitterclone.model.graphql.arguments.{PageSizeArg, PostedBeforeArg}
import twitterclone.model.graphql.types.{LocalDateTimeType, UUIDType}
import twitterclone.repositories.domain.AllRepositories

object TweetType {

  def apply[F[_]]: ObjectType[AllRepositories[F], Tweet] =
    ObjectType(
      name = "Tweet",
      fieldsFn = () => fields(
        Field(
          name = "id",
          fieldType = UUIDType,
          resolve = _.value.id.value
        ),
        Field(
          name = "authorId",
          fieldType = UUIDType,
          resolve = _.value.authorId.value
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
          name = "author",
          fieldType = OptionType(UserType[F]),
          resolve = { context =>
            DeferredType.UserById(context.value.authorId)
          }
        ),
        Field(
          name = "comments",
          fieldType = ListType(CommentType[F]),
          arguments = PageSizeArg :: PostedBeforeArg :: Nil,
          resolve = { context =>
            DeferredType.CommentsByTweetId(
              tweetId = context.value.id,
              pagination = Pagination(
                pageSize = (context arg PageSizeArg) getOrElse 20,
                postedBefore = context arg PostedBeforeArg
              )
            )
          }
        )
      )
    )

}

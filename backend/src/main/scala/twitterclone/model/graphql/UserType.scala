package twitterclone.model.graphql

import sangria.schema._
import twitterclone.model.graphql.arguments.{PageSizeArg, PostedBeforeArg}
import twitterclone.model.{CommentPagination, TweetPagination}
import twitterclone.model.user.User
import twitterclone.repositories.domain.AllRepositories
import twitterclone.model.graphql.types.UUIDType

object UserType {

  def apply[F[_]]: ObjectType[AllRepositories[F], User] =
    ObjectType(
      name = "User",
      fieldsFn = () => fields(
        Field(
          name = "id",
          fieldType = UUIDType,
          resolve = _.value.id.value
        ),
        Field(
          name = "handle",
          fieldType = StringType,
          resolve = _.value.handle.value
        ),
        Field(
          name = "name",
          fieldType = StringType,
          resolve = _.value.name.value
        ),
        Field(
          name = "tweets",
          fieldType = ListType(TweetType[F]),
          arguments = PageSizeArg :: PostedBeforeArg :: Nil,
          resolve = { context =>
            DeferredType.TweetsByUserId(
              userId = context.value.id,
              pagination = TweetPagination(
                pageSize = (context arg PageSizeArg) getOrElse 20,
                postedBefore = context arg PostedBeforeArg
              )
            )
          }
        ),
        Field(
          name = "comments",
          fieldType = ListType(CommentType[F]),
          arguments = PageSizeArg :: PostedBeforeArg :: Nil,
          resolve = { context =>
            DeferredType.CommentsByUserId(
              userId = context.value.id,
              pagination = CommentPagination(
                pageSize = (context arg PageSizeArg) getOrElse 20,
                postedBefore = context arg PostedBeforeArg
              )
            )
          }
        )
      )
    )

}

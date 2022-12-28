package twitterclone.model.graphql

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import sangria.schema._
import twitterclone.model.{Comment, Id, Tweet}
import twitterclone.model.graphql.arguments.UUIDArg
import twitterclone.repositories.domain.AllRepositories

object QueryType {

  def apply(implicit ior: IORuntime): ObjectType[AllRepositories[IO], Unit] =
    ObjectType(
      name = "Query",
      fieldsFn = () => fields[AllRepositories[IO], Unit](
        Field(
          name = "tweet",
          description = Some("Returns the tweet with the specified `id`"),
          fieldType = OptionType(TweetType[IO]),
          arguments = UUIDArg :: Nil,
          resolve = { context =>
            val tweetId = Id[Tweet](context arg UUIDArg)
            context.ctx.tweets.get(tweetId).unsafeToFuture()
          }
        ),
        Field(
          name = "comment",
          description = Some("Returns the comment with the specified `id`"),
          fieldType = OptionType(CommentType[IO]),
          arguments = UUIDArg :: Nil,
          resolve = { context =>
            val commentId = Id[Comment](context arg UUIDArg)
            context.ctx.comments.get(commentId).unsafeToFuture()
          }
        )
      )
    )

  def schema(implicit ior: IORuntime): Schema[AllRepositories[IO], Unit] =
    Schema(QueryType.apply)

}

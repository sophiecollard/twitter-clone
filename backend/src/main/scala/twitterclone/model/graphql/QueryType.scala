package twitterclone.model.graphql

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import sangria.schema._
import twitterclone.model.{Comment, CommentPagination, Id, Tweet, TweetPagination}
import twitterclone.model.graphql.arguments.{PageSizeArg, PostedBeforeArg, TweetIdArg, UUIDArg}
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
          name = "tweets",
          description = Some("Returns a paginated list of tweets"),
          fieldType = ListType(TweetType[IO]),
          arguments = PageSizeArg :: PostedBeforeArg :: Nil,
          resolve = { context =>
            val pagination = TweetPagination(
              pageSize = (context arg PageSizeArg) getOrElse 20,
              postedBefore = context arg PostedBeforeArg
            )
            context.ctx.tweets.list(pagination).unsafeToFuture()
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
        ),
        Field(
          name = "comments",
          description = Some("Returns a paginated list of comments for the specified tweet `id`"),
          fieldType = ListType(CommentType[IO]),
          arguments = TweetIdArg :: PageSizeArg :: PostedBeforeArg :: Nil,
          resolve = { context =>
            val tweetId = Id[Tweet](context arg TweetIdArg)
            val pagination = CommentPagination(
              pageSize = (context arg PageSizeArg) getOrElse 20,
              postedBefore = context arg PostedBeforeArg
            )
            context.ctx.comments.list(tweetId, pagination).unsafeToFuture()
          }
        )
      )
    )

  def schema(implicit ior: IORuntime): Schema[AllRepositories[IO], Unit] =
    Schema(QueryType.apply)

}

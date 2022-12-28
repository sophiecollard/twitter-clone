package twitterclone.services

import twitterclone.services.comment.CommentService
import twitterclone.services.tweet.TweetService

final case class AllServices[F[_]](
  tweets: TweetService[F],
  comments: CommentService[F]
)

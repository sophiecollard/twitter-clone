package twitterclone.repositories.domain

final case class AllRepositories[F[_]](
  tweets: TweetRepository[F],
  comments: CommentRepository[F],
  users: UserRepository[F]
)

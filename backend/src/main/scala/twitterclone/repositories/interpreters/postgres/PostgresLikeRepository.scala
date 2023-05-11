package twitterclone.repositories.interpreters.postgres

import doobie.ConnectionIO
import eu.timepit.refined.types.numeric.NonNegInt
import twitterclone.model.user.User
import twitterclone.model.{Id, Tweet}
import twitterclone.repositories.domain.LikeRepository

object PostgresLikeRepository extends LikeRepository[ConnectionIO] {

  override def likeTweet(tweetId: Id[Tweet], userId: Id[User]): ConnectionIO[Unit] =
    ???

  override def unlikeTweet(tweetId: Id[Tweet], userId: Id[User]): ConnectionIO[Unit] =
    ???

  override def getLikeCount(tweetId: Id[Tweet]): ConnectionIO[NonNegInt] =
    ???

  override def didUserLike(tweetId: Id[Tweet], userId: Id[User]): ConnectionIO[Boolean] =
    ???

}

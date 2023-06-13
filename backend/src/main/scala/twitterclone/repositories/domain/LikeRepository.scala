package twitterclone.repositories.domain

import eu.timepit.refined.types.numeric.NonNegInt
import twitterclone.model.user.User
import twitterclone.model.{Id, Tweet, TweetReaction}

trait LikeRepository[F[_]] {

  def likeTweet(tweetId: Id[Tweet], userId: Id[User]): F[Unit]

  def unlikeTweet(tweetId: Id[Tweet], userId: Id[User]): F[Unit]

  def getLikeCount(tweetId: Id[Tweet]): F[NonNegInt]

  def getUserReaction(tweetId: Id[Tweet], userId: Id[User]): F[TweetReaction]

}

package twitterclone.repositories.interpreters.postgres

import doobie.{ConnectionIO, Query0, Update0}
import doobie.implicits._
import doobie.refined.implicits._
import eu.timepit.refined.types.numeric.NonNegInt
import twitterclone.model.user.User
import twitterclone.model.{Id, Tweet, TweetReaction}
import twitterclone.repositories.domain.LikeRepository
import twitterclone.repositories.interpreters.postgres.instances._

object PostgresLikeRepository extends LikeRepository[ConnectionIO] {

  override def likeTweet(tweetId: Id[Tweet], userId: Id[User]): ConnectionIO[Int] =
    likeTweetUpdate(tweetId, userId).run

  override def unlikeTweet(tweetId: Id[Tweet], userId: Id[User]): ConnectionIO[Int] =
    unlikeTweetUpdate(tweetId, userId).run

  override def getLikeCount(tweetId: Id[Tweet]): ConnectionIO[NonNegInt] =
    getLikeCountQuery(tweetId).unique

  override def getUserReaction(tweetId: Id[Tweet], userId: Id[User]): ConnectionIO[TweetReaction] =
    getUserReactionQuery(tweetId, userId).unique.map { userLikedTweet =>
      if (userLikedTweet) TweetReaction.Liked
      else TweetReaction.NoReaction
    }

  private def likeTweetUpdate(tweetId: Id[Tweet], userId: Id[User]): Update0 =
    sql"""INSERT INTO tweet_likes (tweet_id, user_id)
         |VALUES ($tweetId, $userId)
         |ON CONFLICT DO NOTHING
         |""".stripMargin.update

  private def unlikeTweetUpdate(tweetId: Id[Tweet], userId: Id[User]): Update0 =
    sql"""DELETE
         |FROM tweet_likes
         |WHERE tweet_id = $tweetId
         |AND user_id = $userId
         |""".stripMargin.update

  private def getLikeCountQuery(tweetId: Id[Tweet]): Query0[NonNegInt] =
    sql"""SELECT COUNT(*)
         |FROM tweet_likes
         |WHERE tweet_id = $tweetId
         |""".stripMargin.query[NonNegInt]

  private def getUserReactionQuery(tweetId: Id[Tweet], userId: Id[User]): Query0[Boolean] =
    sql"""SELECT EXISTS (
          |  SELECT tweet_id, user_id
          |  FROM tweet_likes
          |  WHERE tweet_id = $tweetId
          |  AND user_id = $userId
          |)
         |""".stripMargin.query[Boolean]

}

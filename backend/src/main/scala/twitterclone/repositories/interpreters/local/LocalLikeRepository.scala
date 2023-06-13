package twitterclone.repositories.interpreters.local

import cats.Applicative
import cats.implicits._
import eu.timepit.refined.auto._
import eu.timepit.refined.types.numeric.NonNegInt
import twitterclone.model.{Id, Tweet, TweetReaction}
import twitterclone.model.user.User
import twitterclone.repositories.domain.LikeRepository

import scala.collection.mutable

object LocalLikeRepository{

  def apply[F[_]: Applicative](state: mutable.Set[Entry] = mutable.Set.empty): LikeRepository[F] =
    new LikeRepository[F] {
      override def likeTweet(tweetId: Id[Tweet], userId: Id[User]): F[Int] =
        state.add(Entry(tweetId, userId)).pure[F].map { created =>
          if (created) 1 else 0
        }

      override def unlikeTweet(tweetId: Id[Tweet], userId: Id[User]): F[Int] =
        state.remove(Entry(tweetId, userId)).pure[F].map { deleted =>
          if (deleted) 1 else 0
        }

      override def getLikeCount(tweetId: Id[Tweet]): F[NonNegInt] =
        NonNegInt
          .from(state.count(_.tweetId == tweetId))
          .getOrElse[NonNegInt](0)
          .pure[F]

      override def getUserReaction(tweetId: Id[Tweet], userId: Id[User]): F[TweetReaction] = {
        val interaction = if (state.contains(Entry(tweetId, userId))) TweetReaction.Liked
        else TweetReaction.NoReaction
        interaction.pure[F]
      }
    }

  final case class Entry(tweetId: Id[Tweet], userId: Id[User])

}

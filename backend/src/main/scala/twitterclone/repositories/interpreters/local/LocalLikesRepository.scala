package twitterclone.repositories.interpreters.local

import cats.Applicative
import cats.implicits._
import eu.timepit.refined.auto._
import eu.timepit.refined.types.numeric.NonNegInt
import twitterclone.model.{Id, Tweet}
import twitterclone.model.user.User
import twitterclone.repositories.domain.LikesRepository

import scala.collection.mutable

object LocalLikesRepository{

  final case class Like(tweetId: Id[Tweet], userId: Id[User])

  def apply[F[_]: Applicative](state: mutable.Set[Like] = mutable.Set.empty): LikesRepository[F] =
    new LikesRepository[F] {
      override def likeTweet(tweetId: Id[Tweet], userId: Id[User]): F[Unit] =
        state.add(Like(tweetId, userId)).pure[F].void

      override def unlikeTweet(tweetId: Id[Tweet], userId: Id[User]): F[Unit] =
        state.remove(Like(tweetId, userId)).pure[F].void

      override def getLikeCount(tweetId: Id[Tweet]): F[NonNegInt] =
        NonNegInt
          .from(state.count(_.tweetId == tweetId))
          .getOrElse[NonNegInt](0)
          .pure[F]

      override def didUserLike(tweetId: Id[Tweet], userId: Id[User]): F[Boolean] =
        state.contains(Like(tweetId, userId)).pure[F]
    }

}

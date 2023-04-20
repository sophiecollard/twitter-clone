package twitterclone.repositories.interpreters.local

import cats.Applicative
import cats.implicits._
import twitterclone.model.user.User
import twitterclone.model.{Id, Tweet, Pagination}
import twitterclone.repositories.domain.TweetRepository

import java.time.LocalDateTime
import scala.collection.concurrent.TrieMap

/** An implementation of TweetRepository which stores data in memory. */
object LocalTweetRepository {

  def create[F[_]: Applicative](state: TrieMap[Id[Tweet], Tweet] = TrieMap.empty): TweetRepository[F] =
    new TweetRepository[F] {
      override def create(tweet: Tweet): F[Int] =
        if (state.contains(tweet.id))
          0.pure[F]
        else
          state.addOne((tweet.id, tweet)).pure[F].map(_ => 1)

      override def delete(id: Id[Tweet]): F[Int] =
        if (state.contains(id))
          state.remove(id).pure[F].map(_ => 1)
        else
          0.pure[F]

      override def get(id: Id[Tweet]): F[Option[Tweet]] =
        state.get(id).pure[F]

      override def getAuthorId(id: Id[Tweet]): F[Option[Id[User]]] =
        state.get(id).map(_.authorId).pure[F]

      override def list(pagination: Pagination): F[List[Tweet]] =
        state
          .values
          .filter { t => pagination.postedBefore.forall(t.postedOn isBefore _) }
          .toList
          .sortBy(_.postedOn)(Ordering[LocalDateTime].reverse)
          .take(pagination.pageSize)
          .pure[F]

      override def listBy(authorId: Id[User], pagination: Pagination): F[List[Tweet]] =
        state
          .values
          .filter { t => t.authorId == authorId && pagination.postedBefore.forall(t.postedOn isBefore _) }
          .toList
          .sortBy(_.postedOn)(Ordering[LocalDateTime].reverse)
          .take(pagination.pageSize)
          .pure[F]
    }

}

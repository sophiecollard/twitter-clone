package twitterclone.repositories.comment

import cats.Applicative
import cats.implicits._
import twitterclone.model.{Comment, CommentPagination, Id, Tweet, User}

import java.time.LocalDateTime
import scala.collection.concurrent.TrieMap

/** An implementation of CommentRepository which stores data in memory. */
object LocalCommentRepository {

  def create[F[_]: Applicative](state: TrieMap[Id[Comment], Comment] = TrieMap.empty): CommentRepository[F] =
    new CommentRepository[F] {
      override def create(comment: Comment): F[Int] =
        if (state.contains(comment.id))
          0.pure[F]
        else
          state.addOne((comment.id, comment)).pure[F].map(_ => 1)

      override def delete(id: Id[Comment]): F[Int] =
        if (state.contains(id))
          state.remove(id).pure[F].map(_ => 1)
        else
          0.pure[F]

      override def get(id: Id[Comment]): F[Option[Comment]] =
        state.get(id).pure[F]

      override def getAuthor(id: Id[Comment]): F[Option[Id[User]]] =
        state.get(id).map(_.author).pure[F]

      override def list(tweetId: Id[Tweet], pagination: CommentPagination): F[List[Comment]] =
        state
          .values
          .filter { c => c.tweetId == tweetId && pagination.postedBefore.forall(c.postedOn isBefore _) }
          .toList
          .sortBy(_.postedOn)(Ordering[LocalDateTime].reverse)
          .take(pagination.pageSize)
          .pure[F]
    }

}

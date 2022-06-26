package twitterclone.repositories.comment

import cats.Applicative
import cats.implicits._
import twitterclone.model.{Comment, CommentPagination, Id, Tweet, User}

import java.time.ZonedDateTime
import scala.collection.concurrent.TrieMap

trait CommentRepository[F[_]] {

  def create(comment: Comment): F[Int]

  def delete(id: Id[Comment]): F[Int]

  def get(id: Id[Comment]): F[Option[Comment]]

  def getAuthor(id: Id[Comment]): F[Option[Id[User]]]

  def list(tweetId: Id[Tweet], pagination: CommentPagination): F[List[Comment]]

}

object CommentRepository {

  def local[F[_]: Applicative](state: TrieMap[Id[Comment], Comment] = TrieMap.empty): CommentRepository[F] =
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
          .filter { c => c.tweetId == tweetId && (c.postedOn isAfter pagination.postedAfter) }
          .toList
          .sortBy(_.postedOn)(Ordering[ZonedDateTime].reverse)
          .take(pagination.pageSize)
          .pure[F]
    }

}

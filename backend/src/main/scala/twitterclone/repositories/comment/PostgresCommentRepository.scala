package twitterclone.repositories.comment

import doobie.ConnectionIO
import twitterclone.model.user.User
import twitterclone.model.{Comment, CommentPagination, Id, Tweet}

/** An implementation of CommentRepository which stores data in a PostgreSQL DB. */
object PostgresCommentRepository {

  def create: CommentRepository[ConnectionIO] =
    new CommentRepository[ConnectionIO] {
      override def create(comment: Comment): ConnectionIO[Int] =
        ???

      override def delete(id: Id[Comment]): ConnectionIO[Int] =
        ???

      override def get(id: Id[Comment]): ConnectionIO[Option[Comment]] =
        ???

      override def getAuthor(id: Id[Comment]): ConnectionIO[Option[Id[User]]] =
        ???

      override def list(tweetId: Id[Tweet], pagination: CommentPagination): ConnectionIO[List[Comment]] =
        ???
    }

}

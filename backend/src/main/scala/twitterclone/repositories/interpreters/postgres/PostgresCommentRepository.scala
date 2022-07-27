package twitterclone.repositories.interpreters.postgres

import doobie.implicits._
import doobie.implicits.javatimedrivernative._
import doobie.{ConnectionIO, Query0, Update, Update0}
import twitterclone.model.user.User
import twitterclone.model.{Comment, CommentPagination, Id, Tweet}
import twitterclone.repositories.domain.CommentRepository
import twitterclone.repositories.interpreters.postgres.instances._

/** An implementation of CommentRepository which stores data in a PostgreSQL DB. */
object PostgresCommentRepository {

  def create: CommentRepository[ConnectionIO] =
    new CommentRepository[ConnectionIO] {
      override def create(comment: Comment): ConnectionIO[Int] =
        createUpdate.run(comment)

      override def delete(id: Id[Comment]): ConnectionIO[Int] =
        deleteUpdate(id).run

      override def get(id: Id[Comment]): ConnectionIO[Option[Comment]] =
        getQuery(id).option

      override def getAuthor(id: Id[Comment]): ConnectionIO[Option[Id[User]]] =
        getAuthorQuery(id).option

      override def list(tweetId: Id[Tweet], pagination: CommentPagination): ConnectionIO[List[Comment]] =
        listQuery(tweetId, pagination).to[List]
    }

  private val createUpdate: Update[Comment] =
    Update(
      s"""INSERT INTO comments (id, author, tweet_id, contents, posted_on)
         |VALUES (?, ?, ?, ?, ?)
         |ON CONFLICT DO NOTHING
         |""".stripMargin
    )

  private def deleteUpdate(id: Id[Comment]): Update0 =
    sql"""DELETE
         |FROM comments
         |WHERE id = $id
         |""".stripMargin.update

  private def getQuery(id: Id[Comment]): Query0[Comment] =
    ???

  private def getAuthorQuery(id: Id[Comment]): Query0[Id[User]] =
    sql"""SELECT author
         |FROM comments
         |WHERE id = $id
         |""".stripMargin.query[Id[User]]

  private def listQuery(tweetId: Id[Tweet], pagination: CommentPagination): Query0[Comment] =
    ???

}

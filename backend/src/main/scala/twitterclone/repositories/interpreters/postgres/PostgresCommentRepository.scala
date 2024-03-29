package twitterclone.repositories.interpreters.postgres

import doobie.implicits._
import doobie.implicits.javatimedrivernative._
import doobie.{ConnectionIO, Query0, Update, Update0}
import twitterclone.model.user.User
import twitterclone.model.{Comment, Id, Pagination, Tweet}
import twitterclone.repositories.domain.CommentRepository
import twitterclone.repositories.interpreters.postgres.instances._

import java.time.{LocalDateTime, ZoneId}

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

      override def list(tweetId: Id[Tweet], pagination: Pagination): ConnectionIO[List[Comment]] =
        listQuery(tweetId, pagination).to[List]

      override def listBy(authorId: Id[User], pagination: Pagination): ConnectionIO[List[Comment]] =
        listByQuery(authorId, pagination).to[List]
    }

  private val createUpdate: Update[Comment] =
    Update(
      s"""INSERT INTO comments (id, author_id, tweet_id, contents, posted_on)
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
    sql"""SELECT id, author_id, tweet_id, contents, posted_on
         |FROM comments
         |WHERE id= $id
         |""".stripMargin.query[Comment]

  private def getAuthorQuery(id: Id[Comment]): Query0[Id[User]] =
    sql"""SELECT author_id
         |FROM comments
         |WHERE id = $id
         |""".stripMargin.query[Id[User]]

  private def listQuery(tweetId: Id[Tweet], pagination: Pagination): Query0[Comment] =
    sql"""SELECT id, author_id, tweet_id, contents, posted_on
          |FROM comments
          |WHERE tweet_id = $tweetId
          |AND posted_on  < ${pagination.postedBefore.getOrElse(LocalDateTime.now(ZoneId.of("UTC")))}
          |ORDER BY posted_on DESC
          |LIMIT ${pagination.pageSize}
          |""".stripMargin.query[Comment]

  private def listByQuery(authorId: Id[User], pagination: Pagination): Query0[Comment] =
    sql"""SELECT id, author_id, tweet_id, contents, posted_on
         |FROM comments
         |WHERE author_id = $authorId
         |AND posted_on < ${pagination.postedBefore.getOrElse(LocalDateTime.now(ZoneId.of("UTC")))}
         |ORDER BY posted_on DESC
         |LIMIT ${pagination.pageSize}
         |""".stripMargin.query[Comment]

}

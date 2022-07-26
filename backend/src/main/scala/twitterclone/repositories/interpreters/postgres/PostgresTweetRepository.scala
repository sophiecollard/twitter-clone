package twitterclone.repositories.interpreters.postgres

import doobie.implicits._
import doobie.implicits.javatimedrivernative._
import doobie.{ConnectionIO, Query0, Update, Update0}
import twitterclone.model.user.User
import twitterclone.model.{Id, Tweet, TweetPagination}
import twitterclone.repositories.domain.TweetRepository
import twitterclone.repositories.interpreters.postgres.instances._

import java.time.{LocalDateTime, ZoneId}

/** An implementation of TweetRepository which stores data in a PostgreSQL DB. */
object PostgresTweetRepository {

  def create: TweetRepository[ConnectionIO] = new TweetRepository[ConnectionIO] {
    override def create(tweet: Tweet): ConnectionIO[Int] =
      createUpdate.run(tweet)

    override def delete(id: Id[Tweet]): ConnectionIO[Int] =
      deleteUpdate(id).run

    override def get(id: Id[Tweet]): ConnectionIO[Option[Tweet]] =
      getQuery(id).option

    override def getAuthor(id: Id[Tweet]): ConnectionIO[Option[Id[User]]] =
      getAuthorQuery(id).option

    override def list(pagination: TweetPagination): ConnectionIO[List[Tweet]] =
      listQuery(pagination).to[List]

    override def listBy(author: Id[User], pagination: TweetPagination): ConnectionIO[List[Tweet]] =
      listByQuery(author, pagination).to[List]
  }

  private val createUpdate: Update[Tweet] =
    Update(
      s"""INSERT INTO tweets (id, author, contents, posted_on)
         |VALUES (?, ?, ?, ?)
         |ON CONFLICT id DO NOTHING
         |""".stripMargin
    )

  private def deleteUpdate(id: Id[Tweet]): Update0 =
    sql"""DELETE
         |FROM tweets
         |WHERE id = $id
         |""".stripMargin.update

  private def getQuery(id: Id[Tweet]): Query0[Tweet] =
    sql"""SELECT id, author, contents, posted_on
         |FROM tweets
         |WHERE id = $id
         |""".stripMargin.query[Tweet]

  private def getAuthorQuery(id: Id[Tweet]): Query0[Id[User]] =
    sql"""SELECT author
         |FROM tweets
         |WHERE id = $id
         |""".stripMargin.query[Id[User]]

  private def listQuery(pagination: TweetPagination): Query0[Tweet] =
    sql"""SELECT id, author, contents, posted_on
         |FROM tweets
         |WHERE posted_on < ${pagination.postedBefore.getOrElse(LocalDateTime.now(ZoneId.of("UTC")))}
         |LIMIT ${pagination.pageSize}
         |""".stripMargin.query[Tweet]

  private def listByQuery(author: Id[User], pagination: TweetPagination): Query0[Tweet] =
    sql"""SELECT id, author, contents, posted_on
         |FROM tweets
         |WHERE posted_on < ${pagination.postedBefore.getOrElse(LocalDateTime.now(ZoneId.of("UTC")))}
         |AND author = $author
         |LIMIT ${pagination.pageSize}
         |""".stripMargin.query[Tweet]

}

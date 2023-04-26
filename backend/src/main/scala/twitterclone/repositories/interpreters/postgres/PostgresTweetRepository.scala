package twitterclone.repositories.interpreters.postgres

import doobie.implicits._
import doobie.implicits.javatimedrivernative._
import doobie.refined.implicits._
import doobie.{ConnectionIO, Query0, Update0}
import twitterclone.model.user.User
import twitterclone.model.{Id, Tweet, Pagination}
import twitterclone.repositories.domain.TweetRepository
import twitterclone.repositories.interpreters.postgres.instances._

import java.time.{LocalDateTime, ZoneId}

/** An implementation of TweetRepository which stores data in a PostgreSQL DB. */
object PostgresTweetRepository {

  def create: TweetRepository[ConnectionIO] = new TweetRepository[ConnectionIO] {
    override def create(tweet: Tweet): ConnectionIO[Int] =
      createTweet(tweet).run

    override def delete(id: Id[Tweet]): ConnectionIO[Int] =
      deleteUpdate(id).run

    override def get(id: Id[Tweet]): ConnectionIO[Option[Tweet]] =
      getQuery(id).option

    override def getAuthorId(id: Id[Tweet]): ConnectionIO[Option[Id[User]]] =
      getAuthorQuery(id).option

    override def list(pagination: Pagination): ConnectionIO[List[Tweet]] =
      listQuery(pagination).to[List]

    override def listBy(authorId: Id[User], pagination: Pagination): ConnectionIO[List[Tweet]] =
      listByQuery(authorId, pagination).to[List]
  }

  private def createTweet(tweet: Tweet): Update0 =
    sql"""INSERT INTO tweets (id, author_id, contents, posted_on)
         |VALUES (${tweet.id}, ${tweet.authorId}, ${tweet.contents}, ${tweet.postedOn})
         |ON CONFLICT DO NOTHING
         |""".stripMargin.update

  private def deleteUpdate(id: Id[Tweet]): Update0 =
    sql"""DELETE
         |FROM tweets
         |WHERE id = $id
         |""".stripMargin.update

  private def getQuery(id: Id[Tweet]): Query0[Tweet] =
    sql"""SELECT id, author_id, contents, posted_on, 0
         |FROM tweets
         |WHERE id = $id
         |""".stripMargin.query[Tweet]

  private def getAuthorQuery(id: Id[Tweet]): Query0[Id[User]] =
    sql"""SELECT author_id
         |FROM tweets
         |WHERE id = $id
         |""".stripMargin.query[Id[User]]

  private def listQuery(pagination: Pagination): Query0[Tweet] =
    sql"""SELECT id, author_id, contents, posted_on, 0
         |FROM tweets
         |WHERE posted_on < ${pagination.postedBefore.getOrElse(LocalDateTime.now(ZoneId.of("UTC")))}
         |ORDER BY posted_on DESC
         |LIMIT ${pagination.pageSize}
         |""".stripMargin.query[Tweet]

  private def listByQuery(authorId: Id[User], pagination: Pagination): Query0[Tweet] =
    sql"""SELECT id, author_id, contents, posted_on, 0
         |FROM tweets
         |WHERE posted_on < ${pagination.postedBefore.getOrElse(LocalDateTime.now(ZoneId.of("UTC")))}
         |AND author_id = $authorId
         |ORDER BY posted_on DESC
         |LIMIT ${pagination.pageSize}
         |""".stripMargin.query[Tweet]

}

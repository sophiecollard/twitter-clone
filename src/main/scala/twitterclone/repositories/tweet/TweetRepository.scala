package twitterclone.repositories.tweet

import doobie.implicits._
import doobie.{ConnectionIO, Query0, Update, Update0}
import twitterclone.model.{Id, Tweet, TweetPagination, User}
import twitterclone.repositories.shared.instances._

trait TweetRepository[F[_]] {

  def create(tweet: Tweet): F[Int]

  def delete(id: Id[Tweet]): F[Int]

  def get(id: Id[Tweet]): F[Option[Tweet]]

  def getAuthor(id: Id[Tweet]): F[Option[Id[User]]]

  def list(author: Id[User], pagination: TweetPagination): F[List[Tweet]]

}

object TweetRepository {

  def create: TweetRepository[ConnectionIO] = new TweetRepository[ConnectionIO] {
    override def create(tweet: Tweet): ConnectionIO[Int] =
      createUpdate.run(tweet)

    override def delete(id: Id[Tweet]): ConnectionIO[Int] =
      deleteUpdate(id).run

    override def get(id: Id[Tweet]): ConnectionIO[Option[Tweet]] =
      getQuery(id).option

    override def getAuthor(id: Id[Tweet]): ConnectionIO[Option[Id[User]]] =
      getAuthorQuery(id).option

    override def list(author: Id[User], pagination: TweetPagination): ConnectionIO[List[Tweet]] =
      listQuery(author, pagination).to[List]
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

  private def listQuery(author: Id[User], pagination: TweetPagination): Query0[Tweet] =
    sql"""SELECT id, author, contents, posted_on
         |FROM tweets
         |WHERE author = $author
         |AND posted_on > ${pagination.postedAfter}
         |LIMIT ${pagination.pageSize}
         |""".stripMargin.query[Tweet]

}

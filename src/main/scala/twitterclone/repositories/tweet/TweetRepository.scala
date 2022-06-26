package twitterclone.repositories.tweet

import cats.Applicative
import cats.implicits._
import doobie.implicits._
import doobie.{ConnectionIO, Query0, Update, Update0}
import twitterclone.model.{Id, Tweet, TweetPagination, User}
import twitterclone.repositories.shared.instances._

import java.time.ZonedDateTime
import scala.collection.concurrent.TrieMap

trait TweetRepository[F[_]] {

  def create(tweet: Tweet): F[Int]

  def delete(id: Id[Tweet]): F[Int]

  def get(id: Id[Tweet]): F[Option[Tweet]]

  def getAuthor(id: Id[Tweet]): F[Option[Id[User]]]

  def list(author: Id[User], pagination: TweetPagination): F[List[Tweet]]

}

object TweetRepository {

  def local[F[_]: Applicative](state: TrieMap[Id[Tweet], Tweet] = TrieMap.empty): TweetRepository[F] =
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

      override def getAuthor(id: Id[Tweet]): F[Option[Id[User]]] =
        state.get(id).map(_.author).pure[F]

      override def list(author: Id[User], pagination: TweetPagination): F[List[Tweet]] =
        state
          .values
          .filter { t =>t.author == author && pagination.postedAfter.forall(t.postedOn isAfter _) }
          .toList
          .sortBy(_.postedOn)(Ordering[ZonedDateTime].reverse)
          .take(pagination.pageSize)
          .pure[F]
    }

  def postgres: TweetRepository[ConnectionIO] = new TweetRepository[ConnectionIO] {
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
         |AND posted_on > ${pagination.postedAfter.getOrElse(ZonedDateTime.now())}
         |LIMIT ${pagination.pageSize}
         |""".stripMargin.query[Tweet]

}

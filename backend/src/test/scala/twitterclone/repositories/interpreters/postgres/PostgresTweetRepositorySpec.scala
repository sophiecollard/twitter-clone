package twitterclone.repositories.interpreters.postgres

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie.implicits._
import doobie.implicits.javatimedrivernative._
import doobie.{ConnectionIO, Transactor, Update}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import twitterclone.config.PostgresConfig
import twitterclone.fixtures.tweet._
import twitterclone.model.{Tweet, TweetPagination}
import twitterclone.repositories.domain.TweetRepository
import twitterclone.repositories.interpreters.postgres.instances._

class PostgresTweetRepositorySpec
  extends AnyWordSpec
    with PostgresTweetRepositorySetup
    with Matchers
    with BeforeAndAfterEach
    with BeforeAndAfterAll {
  "The create method" should {
    "create a new tweet and return 1" in {
      repo.create(tweet).unsafe shouldBe 1
    }

    "not override an existing tweet" in {
      insert(tweet).unsafe
      repo.create(tweet).unsafe shouldBe 0
    }
  }

  "The delete method" should {
    "delete a tweet" in {
      insert(tweet).unsafe
      repo.delete(tweet.id).unsafe shouldBe 1
    }
  }

  "The getAuthor method" should {
    "get a tweet author's user Id" in {
      insert(tweet).unsafe
      repo.get(tweet.id).unsafe shouldBe Some(tweet)
    }
  }

  "The list method" should {
    "list tweets by decreasing 'postedOn' date" in {
      insertMany(tweet, earlierTweetFromSameAuthor, tweetFromAnotherAuthor).unsafe
      repo.list(TweetPagination.default).unsafe shouldBe
        List(tweet, earlierTweetFromSameAuthor, tweetFromAnotherAuthor)
    }
  }

  "The listBy method" should {
    "list tweets by a given author by decreasing 'postedOn' date" in {
      insertMany(tweet, earlierTweetFromSameAuthor, tweetFromAnotherAuthor).unsafe
      repo.listBy(tweet.author, TweetPagination.default).unsafe shouldBe List(tweet, earlierTweetFromSameAuthor)
    }
  }

  override def beforeAll(): Unit = {
    createTable.unsafe
    super.beforeAll()
  }

  override def beforeEach(): Unit = {
    truncateTable.unsafe
    super.beforeEach()
  }
}

trait PostgresTweetRepositorySetup {

  val postgresConfig: PostgresConfig =
    PostgresConfig.configValue
      .load[IO]
      .unsafeRunSync()

  implicit val xa: Transactor[IO] = utils.getTransactor[IO](postgresConfig)

  val repo: TweetRepository[ConnectionIO] = PostgresTweetRepository.create

  def createTable: ConnectionIO[Int] =
    sql"""CREATE TABLE IF NOT EXISTS tweets (
         |  id        TEXT PRIMARY KEY,
         |  author    TEXT NOT NULL,
         |  contents  TEXT NOT NULL,
         |  posted_on TIMESTAMP NOT NULL
         |);""".stripMargin.update.run

  def truncateTable: ConnectionIO[Int] =
    sql"""TRUNCATE tweets;""".update.run

  val insertUpdate: Update[Tweet] =
    Update(
      """INSERT INTO tweets (id, author, contents, posted_on)
        |VALUES (?, ?, ?, ?)
        |""".stripMargin
    )

  def insert(tweet: Tweet): ConnectionIO[Int] =
    insertUpdate.run(tweet)

  def insertMany(tweets: Tweet*): ConnectionIO[Int] =
    insertUpdate.updateMany(tweets)

  implicit class ConnectionIOOps[A](value: ConnectionIO[A]) {
    def unsafe(implicit xa: Transactor[IO]): A =
      value.transact(xa).unsafeRunSync()
  }

}

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
import twitterclone.repositories.interpreters.postgres.testinstances._

class PostgresTweetRepositorySpec
  extends AnyWordSpec
    with PostgresTweetRepositorySetup
    with Matchers
    with BeforeAndAfterAll
    with BeforeAndAfterEach {
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

  "The get method" should {
    "get a tweet" in {
      insert(tweet).unsafe
      repo.get(tweet.id).unsafe shouldBe Some(tweet)
    }
  }

  "The getAuthor method" should {
    "get a tweet author's user id" in {
      insert(tweet).unsafe
      repo.getAuthor(tweet.id).unsafe shouldBe Some(tweet.author)
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
    "list tweets by a given author by decreasing 'postedOn' timestamp" in {
      insertMany(tweet, earlierTweetFromSameAuthor, tweetFromAnotherAuthor).unsafe
      repo.listBy(tweet.author, TweetPagination.default).unsafe shouldBe List(tweet, earlierTweetFromSameAuthor)
    }
  }

  override def beforeAll(): Unit = {
    super.beforeAll()
    createTable.unsafe
    ()
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    truncateTable.unsafe
    ()
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

  private val insertUpdate: Update[Tweet] =
    Update(
      """INSERT INTO tweets (id, author, contents, posted_on)
        |VALUES (?, ?, ?, ?)
        |""".stripMargin
    )

  def insert(tweet: Tweet): ConnectionIO[Int] =
    insertUpdate.run(tweet)

  def insertMany(tweets: Tweet*): ConnectionIO[Int] =
    insertUpdate.updateMany(tweets)

}

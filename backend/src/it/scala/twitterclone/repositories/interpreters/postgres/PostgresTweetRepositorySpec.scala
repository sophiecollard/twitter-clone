package twitterclone.repositories.interpreters.postgres

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie.implicits._
import doobie.implicits.javatimedrivernative._
import doobie.refined.implicits._
import doobie.{ConnectionIO, Transactor, Update}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import twitterclone.config.PostgresConfig
import twitterclone.fixtures.tweet._
import twitterclone.model.user.User
import twitterclone.model.{Id, Pagination, Tweet}
import twitterclone.repositories.domain.TweetRepository
import twitterclone.repositories.interpreters.postgres.instances._
import twitterclone.repositories.interpreters.postgres.testinstances._
import twitterclone.repositories.interpreters.postgres.utils.runMigrations

import java.time.LocalDateTime

class PostgresTweetRepositorySpec
  extends AnyWordSpec
    with PostgresTweetRepositorySetup
    with Matchers
    with BeforeAndAfterAll
    with BeforeAndAfterEach {
  "The create method" should {
    "create a new tweet and return 1" in {
      repo.create(tweet).unsafe shouldBe 1
      get(tweet.id).unsafe shouldBe Some(tweet)
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
      get(tweet.id).unsafe shouldBe None
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
      repo.getAuthorId(tweet.id).unsafe shouldBe Some(tweet.authorId)
    }
  }

  "The list method" should {
    "list tweets by decreasing 'postedOn' date" in {
      insertMany(tweet, earlierTweetFromSameAuthor, tweetFromAnotherAuthor).unsafe
      repo.list(Pagination.default).unsafe shouldBe
        List(tweet, earlierTweetFromSameAuthor, tweetFromAnotherAuthor)
    }
  }

  "The listBy method" should {
    "list tweets by a given author by decreasing 'postedOn' timestamp" in {
      insertMany(tweet, earlierTweetFromSameAuthor, tweetFromAnotherAuthor).unsafe
      repo.listBy(tweet.authorId, Pagination.default).unsafe shouldBe List(tweet, earlierTweetFromSameAuthor)
    }
  }

  override def beforeAll(): Unit = {
    super.beforeAll()
    runMigrations[IO](postgresConfig).unsafeRunSync()
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

  def truncateTable: ConnectionIO[Int] =
    sql"""TRUNCATE tweets;""".update.run

  private val insertUpdate: Update[(Id[Tweet], Id[User], String, LocalDateTime)] =
    Update(
      """INSERT INTO tweets (id, author_id, contents, posted_on)
        |VALUES (?, ?, ?, ?)
        |""".stripMargin
    )

  def tupled(tweet: Tweet): (Id[Tweet], Id[User], String, LocalDateTime) =
    (tweet.id, tweet.authorId, tweet.contents, tweet.postedOn)

  def insert(tweet: Tweet): ConnectionIO[Int] =
    insertUpdate.run(tupled(tweet))

  def insertMany(tweets: Tweet*): ConnectionIO[Int] =
    insertUpdate.updateMany(tweets.map(tupled))

  def get(id: Id[Tweet]): ConnectionIO[Option[Tweet]] =
    sql"""SELECT id, author_id, contents, posted_on, 0
         |FROM tweets
         |WHERE id = $id
         |""".stripMargin.query[Tweet].option

}

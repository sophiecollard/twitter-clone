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
import twitterclone.repositories.domain.TweetRepository.TweetData
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
      repo.create(tweetData).unsafe shouldBe 1
      get(tweet.id).unsafe shouldBe Some(tweetData)
    }

    "not override an existing tweet" in {
      insert(tweetData).unsafe
      repo.create(tweetData).unsafe shouldBe 0
    }
  }

  "The delete method" should {
    "delete a tweet" in {
      insert(tweetData).unsafe
      repo.delete(tweet.id).unsafe shouldBe 1
      get(tweet.id).unsafe shouldBe None
    }
  }

  "The get method" should {
    "get a tweet" in {
      insert(tweetData).unsafe
      repo.get(tweet.id).unsafe shouldBe Some(tweetData)
    }
  }

  "The getAuthor method" should {
    "get a tweet author's user id" in {
      insert(tweetData).unsafe
      repo.getAuthorId(tweet.id).unsafe shouldBe Some(tweet.authorId)
    }
  }

  "The list method" should {
    "list tweets by decreasing 'postedOn' date" in {
      insertMany(tweetData, earlierTweetFromSameAuthorData, tweetFromAnotherAuthorData).unsafe
      repo.list(Pagination.default).unsafe shouldBe
        List(tweetData, earlierTweetFromSameAuthorData, tweetFromAnotherAuthorData)
    }
  }

  "The listBy method" should {
    "list tweets by a given author by decreasing 'postedOn' timestamp" in {
      insertMany(tweetData, earlierTweetFromSameAuthorData, tweetFromAnotherAuthorData).unsafe
      repo.listBy(tweet.authorId, Pagination.default).unsafe shouldBe List(tweetData, earlierTweetFromSameAuthorData)
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

  def tupled(tweetData: TweetData): (Id[Tweet], Id[User], String, LocalDateTime) =
    (tweetData.id, tweetData.authorId, tweetData.contents, tweetData.postedOn)

  def insert(tweetData: TweetData): ConnectionIO[Int] =
    insertUpdate.run(tupled(tweetData))

  def insertMany(tweetData: TweetData*): ConnectionIO[Int] =
    insertUpdate.updateMany(tweetData.map(tupled))

  def get(id: Id[Tweet]): ConnectionIO[Option[TweetData]] =
    sql"""SELECT id, author_id, contents, posted_on
         |FROM tweets
         |WHERE id = $id
         |""".stripMargin.query[TweetData].option

}

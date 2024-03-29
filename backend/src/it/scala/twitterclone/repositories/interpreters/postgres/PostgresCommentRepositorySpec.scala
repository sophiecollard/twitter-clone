package twitterclone.repositories.interpreters.postgres

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie.implicits._
import doobie.implicits.javatimedrivernative._
import doobie.{ConnectionIO, Transactor, Update}
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatest.wordspec.AnyWordSpec
import twitterclone.config.PostgresConfig
import twitterclone.fixtures.comment._
import twitterclone.model.{Comment, Id, Pagination}
import twitterclone.repositories.domain.CommentRepository
import twitterclone.repositories.interpreters.postgres.instances._
import twitterclone.repositories.interpreters.postgres.testinstances._
import twitterclone.repositories.interpreters.postgres.utils.runMigrations

class PostgresCommentRepositorySpec
  extends AnyWordSpec
    with PostgresCommentRepositorySetup
    with Matchers
    with BeforeAndAfterAll
    with BeforeAndAfterEach {
  "The create method" should {
    "create a new comment and return 1" in {
      repo.create(comment).unsafe shouldBe 1
      get(comment.id).unsafe shouldBe Some(comment)
    }

    "not override an existing comment" in {
      insert(comment).unsafe
      repo.create(comment).unsafe shouldBe 0
    }
  }

  "The delete method" should {
    "delete a comment" in {
      insert(comment).unsafe
      repo.delete(comment.id).unsafe shouldBe 1
      get(comment.id).unsafe shouldBe None
    }
  }

  "The get method" should {
    "get a comment" in {
      insert(comment).unsafe
      repo.get(comment.id).unsafe shouldBe Some(comment)
    }
  }

  "The getAuthor method" should {
    "get a comment author's user id" in {
      insert(comment).unsafe
      repo.getAuthor(comment.id).unsafe shouldBe Some(comment.authorId)
    }
  }

  "The list method" should {
    "list comments for a given tweet by decreasing 'postedOn' timestamp" in {
      insertMany(comment, earlierCommentOnSameTweet, commentOnAnotherTweet).unsafe
      repo.list(comment.tweetId, Pagination.default).unsafe shouldBe List(comment, earlierCommentOnSameTweet)
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

trait PostgresCommentRepositorySetup {

  val postgresConfig: PostgresConfig =
    PostgresConfig.configValue
      .load[IO]
      .unsafeRunSync()

  implicit val xa: Transactor[IO] = utils.getTransactor[IO](postgresConfig)

  val repo: CommentRepository[ConnectionIO] = PostgresCommentRepository.create

  def truncateTable: ConnectionIO[Int] =
    sql"""TRUNCATE comments;""".update.run

  private val insertUpdate: Update[Comment] =
    Update(
      """INSERT INTO comments (id, author_id, tweet_id, contents, posted_on)
        |VALUES (?, ?, ?, ?, ?)
        |""".stripMargin
    )

  def insert(comment: Comment): ConnectionIO[Int] =
    insertUpdate.run(comment)

  def insertMany(comments: Comment*): ConnectionIO[Int] =
    insertUpdate.updateMany(comments)

  def get(id: Id[Comment]): ConnectionIO[Option[Comment]] =
    sql"""SELECT id, author_id, tweet_id, contents, posted_on
         |FROM comments
         |WHERE id = $id
         |""".stripMargin.query[Comment].option

}

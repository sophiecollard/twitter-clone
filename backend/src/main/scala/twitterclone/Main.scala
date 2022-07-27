package twitterclone

import cats.arrow.FunctionK
import cats.effect.{ExitCode, IO, IOApp}
import doobie.ConnectionIO
import fs2.Stream
import org.http4s.server.ServerBuilder
import twitterclone.api.Server
import twitterclone.api.authentication.dummyAuthMiddleware
import twitterclone.api.comment.CommentEndpoints
import twitterclone.api.tweet.TweetEndpoints
import twitterclone.config.Config
import twitterclone.instances.ioTransactor
import repositories.interpreters.postgres.{utils => postgresUtils}
import twitterclone.repositories.interpreters.local.{LocalCommentRepository, LocalTweetRepository}
import twitterclone.repositories.interpreters.postgres.{PostgresCommentRepository, PostgresTweetRepository}
import twitterclone.services.comment.CommentService
import twitterclone.services.tweet.TweetService

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val stream: Stream[IO, ExitCode] = for {
      config <- Stream.eval(Config.configValue.load[IO])
      _ = println(s"Loaded application configuration: $config")
      serverBuilder = config match {
        case c: Config.Local => localServerBuilder(c)
        case c: Config.Production => productionServerBuilder(c)
      }
      _ <- serverBuilder.serve
    } yield ExitCode.Success
    stream.compile.last.map(_.getOrElse(ExitCode.Error))
  }

  private def localServerBuilder(config: Config.Local): ServerBuilder[IO] = {
    val commentRepository = LocalCommentRepository.create[IO]()
    val commentAuthService = services.comment.auth.byAuthor(commentRepository)
    val commentService = CommentService.create(commentRepository, commentAuthService)
    val commentEndpoints = CommentEndpoints.create[IO](dummyAuthMiddleware, commentService)
    val tweetRepository = LocalTweetRepository.create[IO]()
    val tweetAuthService = services.tweet.auth.byAuthor(tweetRepository)
    val tweetService = TweetService.create(tweetRepository, tweetAuthService)
    val tweetEndpoints = TweetEndpoints.create[IO](dummyAuthMiddleware, tweetService)
    Server.builder(config.server, commentEndpoints, tweetEndpoints)
  }

  private def productionServerBuilder(config: Config.Production): ServerBuilder[IO] = {
    val xa = postgresUtils.getTransactor[IO](config.postgres)
    implicit val doobieTransactor: FunctionK[ConnectionIO, IO] = xa.trans

    val commentRepository = PostgresCommentRepository.create
    val commentAuthService = services.comment.auth.byAuthor(commentRepository)
    val commentService = CommentService.create[IO, ConnectionIO](commentRepository, commentAuthService)
    val commentEndpoints = CommentEndpoints.create[IO](dummyAuthMiddleware, commentService)
    val tweetRepository = PostgresTweetRepository.create
    val tweetAuthService = services.tweet.auth.byAuthor(tweetRepository)
    val tweetService = TweetService.create[IO, ConnectionIO](tweetRepository, tweetAuthService)
    val tweetEndpoints = TweetEndpoints.create[IO](dummyAuthMiddleware, tweetService)
    Server.builder(config.server, commentEndpoints, tweetEndpoints)
  }

}

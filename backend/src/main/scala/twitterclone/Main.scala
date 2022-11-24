package twitterclone

import cats.arrow.FunctionK
import cats.effect.{ExitCode, IO, IOApp}
import doobie.ConnectionIO
import fs2.Stream
import org.http4s.server.ServerBuilder
import twitterclone.api.Server
import twitterclone.api.authentication.dummyAuthMiddleware
import twitterclone.api.v1.comment.CommentEndpoints
import twitterclone.api.v1.tweet.TweetEndpoints
import twitterclone.api.v2.SwaggerDocsEndpoints
import twitterclone.api.v2.interpreters.{Http4sCommentEndpoints, Http4sTweetEndpoints}
import twitterclone.config.Config
import twitterclone.instances.ioTransactor
import twitterclone.repositories.interpreters.local.{LocalCommentRepository, LocalTweetRepository}
import twitterclone.repositories.interpreters.postgres.{PostgresCommentRepository, PostgresTweetRepository, utils => postgresUtils}
import twitterclone.services.analytics.AnalyticsService
import twitterclone.services.analytics.publishing.InternetPublisher
import twitterclone.services.comment.CommentService
import twitterclone.services.tweet.TweetService

object Main extends IOApp {

  val analyticsService = new AnalyticsService(InternetPublisher)

  override def run(args: List[String]): IO[ExitCode] = {
    val stream: Stream[IO, ExitCode] = for {
      config <- Stream.eval(Config.configValue.load[IO])
      _ = println(s"Loaded application configuration: $config")
      serverBuilder = config match {
        case c: Config.Local => localServerBuilder(c)
        case c: Config.Production => productionServerBuilder(c)
      }
      _ = analyticsService.registerEvent(analyticsService.ServerStarted)
      _ <- serverBuilder.serve
    } yield ExitCode.Success
    stream.compile.last.map(_.getOrElse(ExitCode.Error))
  }

  private def localServerBuilder(config: Config.Local): ServerBuilder[IO] = {
    val commentRepository = LocalCommentRepository.create[IO]()
    val commentAuthService = services.comment.auth.byAuthor(commentRepository)
    val commentService = CommentService.create(commentRepository, commentAuthService)
    val v1CommentEndpoints = CommentEndpoints.create[IO](dummyAuthMiddleware, commentService)
    val v2CommentEndpoints = Http4sCommentEndpoints.create[IO](commentService)
    val tweetRepository = LocalTweetRepository.create[IO]()
    val tweetAuthService = services.tweet.auth.byAuthor(tweetRepository)
    val tweetService = TweetService.create(tweetRepository, tweetAuthService)
    val v1TweetEndpoints = TweetEndpoints.create[IO](dummyAuthMiddleware, tweetService)
    val v2TweetEndpoints = Http4sTweetEndpoints.create[IO](tweetService)
    val v2SwaggerDocsEndpoints = SwaggerDocsEndpoints.create[IO]
    Server.builder(config.server, v1CommentEndpoints, v1TweetEndpoints, v2CommentEndpoints, v2TweetEndpoints, v2SwaggerDocsEndpoints)
  }

  private def productionServerBuilder(config: Config.Production): ServerBuilder[IO] = {
    val xa = postgresUtils.getTransactor[IO](config.postgres)
    implicit val doobieTransactor: FunctionK[ConnectionIO, IO] = xa.trans

    val commentRepository = PostgresCommentRepository.create
    val commentAuthService = services.comment.auth.byAuthor(commentRepository)
    val commentService = CommentService.create[IO, ConnectionIO](commentRepository, commentAuthService)
    val v1CommentEndpoints = CommentEndpoints.create[IO](dummyAuthMiddleware, commentService)
    val v2CommentEndpoints = Http4sCommentEndpoints.create[IO](commentService)
    val tweetRepository = PostgresTweetRepository.create
    val tweetAuthService = services.tweet.auth.byAuthor(tweetRepository)
    val tweetService = TweetService.create[IO, ConnectionIO](tweetRepository, tweetAuthService)
    val v1TweetEndpoints = TweetEndpoints.create[IO](dummyAuthMiddleware, tweetService)
    val v2TweetEndpoints = Http4sTweetEndpoints.create[IO](tweetService)
    val v2SwaggerDocsEndpoints = SwaggerDocsEndpoints.create[IO]
    Server.builder(config.server, v1CommentEndpoints, v1TweetEndpoints, v2CommentEndpoints, v2TweetEndpoints, v2SwaggerDocsEndpoints)
  }

}

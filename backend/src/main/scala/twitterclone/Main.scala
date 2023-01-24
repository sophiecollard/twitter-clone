package twitterclone

import cats.arrow.FunctionK
import cats.effect.unsafe.IORuntime
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import doobie.ConnectionIO
import eu.timepit.refined.auto._
import fs2.Stream
import org.http4s.server.ServerBuilder
import twitterclone.api.Server
import twitterclone.api.authentication.dummyAuthMiddleware
import twitterclone.config.{Config, PostgresConfig}
import twitterclone.instances.ioTransactor
import repositories.interpreters.postgres.{PostgresCommentRepository, PostgresTweetRepository, PostgresUserRepository, utils => postgresUtils}
import twitterclone.api.graphql.GraphQLEndpoint
import twitterclone.api.v1.comment.CommentApiEndpoints
import twitterclone.api.v1.tweet.TweetApiEndpoints
import twitterclone.api.v2.SwaggerDocsEndpoints
import twitterclone.api.v2.interpreters.{Http4sCommentApiEndpoints, Http4sTweetApiEndpoints}
import twitterclone.model.Id
import twitterclone.model.graphql.{GraphQLDeferredResolver, QueryType}
import twitterclone.model.user.{Handle, Name, Status, User}
import twitterclone.repositories.domain.{AllRepositories, CommentRepository, TweetRepository, UserRepository}
import twitterclone.repositories.interpreters.local.{LocalCommentRepository, LocalTweetRepository, LocalUserRepository}
import twitterclone.services.comment.CommentService
import twitterclone.services.graphql.interpreters.SangriaGraphQLService
import twitterclone.services.tweet.TweetService

import java.util.UUID
import scala.collection.concurrent.TrieMap
import scala.concurrent.ExecutionContext

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val stream: Stream[IO, ExitCode] = for {
      config <- Stream.eval(Config.configValue.load[IO])
      _ = println(s"Loaded application configuration: $config")
      serverBuilder <- Stream.eval(config match {
        case c: Config.Local => localServerBuilder(c)
        case c: Config.Production => productionServerBuilder(c)
      })
      _ <- serverBuilder.serve
    } yield ExitCode.Success
    stream.compile.last.map(_.getOrElse(ExitCode.Error))
  }

  private def localServerBuilder(config: Config.Local): IO[ServerBuilder[IO]] = {
    val commentRepository = LocalCommentRepository.create[IO]()
    val commentAuthService = services.comment.auth.byAuthor(commentRepository)
    val commentService = CommentService.create(commentRepository, commentAuthService)
    val v1CommentApiEndpoints = CommentApiEndpoints[IO](dummyAuthMiddleware[IO], commentService)
    val v2CommentApiEndpoints = Http4sCommentApiEndpoints[IO](commentService)
    val tweetRepository = LocalTweetRepository.create[IO]()
    val tweetAuthService = services.tweet.auth.byAuthor(tweetRepository)
    val tweetService = TweetService.create(tweetRepository, tweetAuthService)
    val v1TweetApiEndpoints = TweetApiEndpoints[IO](dummyAuthMiddleware[IO], tweetService)
    val v2TweetApiEndpoints = Http4sTweetApiEndpoints[IO](tweetService)
    val v2SwaggerDocsEndpoints = SwaggerDocsEndpoints[IO]
    implicit val ior: IORuntime = IORuntime.global
    implicit val ec: ExecutionContext = ior.compute
    val userRepository = LocalUserRepository.create[IO](TrieMap.from(List(testUser.id -> testUser)))
    val allRepositories = AllRepositories(tweetRepository, commentRepository, userRepository)
    val graphQLService = SangriaGraphQLService[IO](QueryType.schema, allRepositories, GraphQLDeferredResolver.apply)
    val graphQLEndpoint = GraphQLEndpoint(graphQLService)
    Server.builder(
      config.server,
      v1CommentApiEndpoints,
      v1TweetApiEndpoints,
      v2CommentApiEndpoints,
      v2TweetApiEndpoints,
      v2SwaggerDocsEndpoints,
      graphQLEndpoint
    ).pure[IO]
  }

  private def productionServerBuilder(config: Config.Production): IO[ServerBuilder[IO]] = {
    implicit val ior: IORuntime = IORuntime.global
    implicit val ec: ExecutionContext = ior.compute
    val xa = postgresUtils.getTransactor[IO](config.postgres)
    implicit val doobieTransactor: FunctionK[ConnectionIO, IO] = xa.trans

    val commentRepository = PostgresCommentRepository.create
    val commentAuthService = services.comment.auth.byAuthor(commentRepository)
    val commentService = CommentService.create[IO, ConnectionIO](commentRepository, commentAuthService)
    val v1CommentEndpoints = CommentApiEndpoints[IO](dummyAuthMiddleware[IO], commentService)
    val v2CommentEndpoints = Http4sCommentApiEndpoints[IO](commentService)
    val tweetRepository = PostgresTweetRepository.create
    val tweetAuthService = services.tweet.auth.byAuthor(tweetRepository)
    val tweetService = TweetService.create[IO, ConnectionIO](tweetRepository, tweetAuthService)
    val v1TweetEndpoints = TweetApiEndpoints[IO](dummyAuthMiddleware[IO], tweetService)
    val v2TweetEndpoints = Http4sTweetApiEndpoints[IO](tweetService)
    val userRepository = PostgresUserRepository.create
    val v2SwaggerDocsEndpoints = SwaggerDocsEndpoints[IO]
    val allRepositories = AllRepositories[IO](
      tweets = TweetRepository.mapF[ConnectionIO, IO](tweetRepository),
      comments = CommentRepository.mapF[ConnectionIO, IO](commentRepository),
      users = UserRepository.mapF[ConnectionIO, IO](userRepository)
    )
    val graphQLService = SangriaGraphQLService[IO](
      schema = QueryType.schema,
      repositories = allRepositories,
      deferredResolver = GraphQLDeferredResolver.apply
    )
    val graphQLEndpoint = GraphQLEndpoint(graphQLService)

    runMigrations(config.postgres).map { _ =>
      Server.builder(
        config.server,
        v1CommentEndpoints,
        v1TweetEndpoints,
        v2CommentEndpoints,
        v2TweetEndpoints,
        v2SwaggerDocsEndpoints,
        graphQLEndpoint
      )
    }
  }

  private def runMigrations(postgresConfig: PostgresConfig): IO[Unit] =
    postgresUtils.runMigrations[IO](postgresConfig).flatMap { migrateResult =>
      if (migrateResult.success)
        IO.delay(println(s"INFO Successfully ran ${migrateResult.migrationsExecuted} migrations"))
      else
        // TODO Abort service start if a migration fails
        IO.delay(println("ERROR Failed to execute migrations"))
    }

  private val testUser: User =
    User(
      id = Id[User](UUID.fromString("0b73e653-5f82-46cd-a232-0166d83ce531")),
      handle = Handle("test_user"),
      name = Name("Test User"),
      status = Status.Active
    )

}

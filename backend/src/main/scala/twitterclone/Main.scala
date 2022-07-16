package twitterclone

import cats.effect.{ExitCode, IO, IOApp}
import fs2.Stream
import org.http4s.server.middleware.CORSConfig
import twitterclone.api.Server
import twitterclone.api.authentication.dummyAuthMiddleware
import twitterclone.api.comment.CommentEndpoints
import twitterclone.api.tweet.TweetEndpoints
import twitterclone.config.ServerConfig
import twitterclone.instances.ioTransactor
import twitterclone.repositories.comment.LocalCommentRepository
import twitterclone.repositories.tweet.LocalTweetRepository
import twitterclone.services.comment.CommentService
import twitterclone.services.tweet.TweetService

import scala.annotation.nowarn

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    @nowarn("cat=deprecation")
    val stream: Stream[IO, ExitCode] = for {
      _ <- Stream.emit(()).covary[IO]
      commentRepository = LocalCommentRepository.create[IO]()
      commentAuthService = services.comment.auth.byAuthor(commentRepository)
      commentService = CommentService.create(commentRepository, commentAuthService)
      commentEndpoints = CommentEndpoints.create[IO](dummyAuthMiddleware, commentService)
      tweetRepository = LocalTweetRepository.create[IO]()
      tweetAuthService = services.tweet.auth.byAuthor(tweetRepository)
      tweetService = TweetService.create(tweetRepository, tweetAuthService)
      tweetEndpoints = TweetEndpoints.create[IO](dummyAuthMiddleware, tweetService)
      corsConfig = CORSConfig.default.withAnyOrigin(false).withAllowedOrigins(Set("http://localhost:8000"))
      serverConfig = ServerConfig("0.0.0.0", 8080, corsConfig)
      serverBuilder = Server.create(serverConfig, commentEndpoints, tweetEndpoints)
      _ <- serverBuilder.serve
    } yield ExitCode.Success
    stream.compile.last.map(_.getOrElse(ExitCode.Error))
  }

}

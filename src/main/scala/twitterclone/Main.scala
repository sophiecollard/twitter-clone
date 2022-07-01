package twitterclone

import cats.arrow.FunctionK
import cats.effect.{ExitCode, IO, IOApp}
import cats.~>
import fs2.Stream
import twitterclone.api.Server
import twitterclone.api.authentication.dummyAuthMiddleware
import twitterclone.api.tweet.TweetEndpoints
import twitterclone.config.ServerConfig
import twitterclone.repositories.tweet.LocalTweetRepository
import twitterclone.services.tweet.TweetService

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val stream: Stream[IO, ExitCode] = for {
      _ <- Stream.emit(()).covary[IO]
      tweetRepository = LocalTweetRepository.create[IO]()
      tweetAuthService = services.tweet.auth.byAuthor(tweetRepository)
      tweetService = TweetService.create[IO, IO](tweetRepository, tweetAuthService)
      tweetEndpoints = TweetEndpoints.create[IO](dummyAuthMiddleware, tweetService)
      serverConfig = ServerConfig("0.0.0.0", 8080)
      serverBuilder = Server.create(serverConfig, tweetEndpoints)
      _ <- serverBuilder.serve
    } yield ExitCode.Success
    stream.compile.last.map(_.getOrElse(ExitCode.Error))
  }

  private implicit val ioTransactor: FunctionK[IO, IO] =
    new ~>[IO, IO] {
      override def apply[A](fa: IO[A]): IO[A] =
        fa
    }

}

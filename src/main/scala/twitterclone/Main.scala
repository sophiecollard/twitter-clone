package twitterclone

import cats.arrow.FunctionK
import cats.data.{Kleisli, OptionT}
import cats.effect.{ExitCode, IO, IOApp}
import cats.~>
import fs2.Stream
import org.http4s.Request
import org.http4s.server.AuthMiddleware
import twitterclone.api.Server
import twitterclone.api.tweet.TweetEndpoints
import twitterclone.config.ServerConfig
import twitterclone.model.{Id, User}
import twitterclone.repositories.tweet.TweetRepository
import twitterclone.services.tweet.TweetService

import java.util.UUID
import scala.util.Try

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val stream: Stream[IO, ExitCode] = for {
      _ <- Stream.emit(()).covary[IO]
      tweetRepository = TweetRepository.local[IO]()
      tweetAuthService = services.tweet.auth.byAuthorId(tweetRepository)
      tweetService = TweetService.create[IO, IO](tweetRepository, tweetAuthService)
      authMiddleware = AuthMiddleware(dummyAuth)
      tweetEndpoints = TweetEndpoints.create[IO](authMiddleware, tweetService)
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

  private val dummyAuth: Kleisli[OptionT[IO, *], Request[IO], Id[User]] =
    Kleisli { request =>
      val maybeUserId: Option[Id[User]] = request
        .headers.headers
        .find(_.name.toString == "x-user-id")
        .flatMap { rawHeader =>
          Try(UUID.fromString(rawHeader.value))
            .map(Id.apply[User])
            .toOption
        }
      OptionT.fromOption[IO](maybeUserId)
    }

}

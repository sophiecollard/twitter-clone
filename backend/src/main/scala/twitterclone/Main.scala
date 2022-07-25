package twitterclone

import cats.effect.{ExitCode, IO, IOApp}
import fs2.Stream
import twitterclone.api.Server
import twitterclone.api.authentication.dummyAuthMiddleware
import twitterclone.api.comment.CommentEndpoints
import twitterclone.api.tweet.TweetEndpoints
import twitterclone.config.Config
import twitterclone.instances.ioTransactor
import twitterclone.repositories.comment.LocalCommentRepository
import twitterclone.repositories.tweet.LocalTweetRepository
import twitterclone.services.comment.CommentService
import twitterclone.services.tweet.TweetService

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val stream: Stream[IO, ExitCode] = for {
      config <- Stream.eval(Config.configValue.load[IO])
      commentRepository = LocalCommentRepository.create[IO]()
      commentAuthService = services.comment.auth.byAuthor(commentRepository)
      commentService = CommentService.create(commentRepository, commentAuthService)
      commentEndpoints = CommentEndpoints.create[IO](dummyAuthMiddleware, commentService)
      tweetRepository = LocalTweetRepository.create[IO]()
      tweetAuthService = services.tweet.auth.byAuthor(tweetRepository)
      tweetService = TweetService.create(tweetRepository, tweetAuthService)
      tweetEndpoints = TweetEndpoints.create[IO](dummyAuthMiddleware, tweetService)
      serverBuilder = Server.create(config.server, commentEndpoints, tweetEndpoints)
      _ <- serverBuilder.serve
    } yield ExitCode.Success
    stream.compile.last.map(_.getOrElse(ExitCode.Error))
  }

}

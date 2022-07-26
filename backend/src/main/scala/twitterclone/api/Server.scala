package twitterclone.api

import cats.effect.Async
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.jetty.server.JettyBuilder
import org.http4s.server.middleware.CORS
import org.http4s.server.{DefaultServiceErrorHandler, Router, ServerBuilder}
import org.http4s.{Http, HttpRoutes}
import twitterclone.api.comment.CommentEndpoints
import twitterclone.api.tweet.TweetEndpoints
import twitterclone.config.ServerConfig

import scala.annotation.nowarn
import scala.concurrent.duration._

object Server {

  def builder[F[_]: Async](
    config: ServerConfig,
    commentEndpoints: CommentEndpoints[F],
    tweetEndpoints: TweetEndpoints[F]
  ): ServerBuilder[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

    @nowarn("cat=deprecation")
    val router: Http[F, F] = Router[F](
      "/live" -> HttpRoutes.of[F] { case GET -> Root => Ok() },
      "/v1/comments" -> CORS(commentEndpoints.httpRoutes, config.cors),
      "/v1/tweets" -> CORS(tweetEndpoints.httpRoutes, config.cors)
    ).orNotFound

    JettyBuilder[F]
      .bindHttp(config.port, config.host)
      .withIdleTimeout(1.minute)
      .withServiceErrorHandler(DefaultServiceErrorHandler)
      .mountService(
        HttpRoutes.of { case request => router.run(request) },
        prefix = ""
      )
      .withBanner(Nil) // TODO check what this does
  }

}

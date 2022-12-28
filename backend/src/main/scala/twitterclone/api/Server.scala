package twitterclone.api

import cats.effect.Async
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.jetty.server.JettyBuilder
import org.http4s.server.{DefaultServiceErrorHandler, Router, ServerBuilder}
import org.http4s.{Http, HttpRoutes}
import twitterclone.api.graphql.GraphQLEndpoint
import twitterclone.api.v1.comment.CommentEndpoints
import twitterclone.api.v1.tweet.TweetEndpoints
import twitterclone.api.v2.SwaggerDocsEndpoints
import twitterclone.api.v2.interpreters.{Http4sCommentEndpoints, Http4sTweetEndpoints}
import twitterclone.config.ServerConfig

import scala.concurrent.duration._

object Server {

  def builder[F[_]: Async](
    config: ServerConfig,
    v1CommentEndpoints: CommentEndpoints[F],
    v1TweetEndpoints: TweetEndpoints[F],
    v2CommentEnpoints: Http4sCommentEndpoints[F],
    v2TweetEndpoints: Http4sTweetEndpoints[F],
    v2SwaggerDocsEndpoints: SwaggerDocsEndpoints[F],
    graphQLEndpoint: GraphQLEndpoint[F]
  ): ServerBuilder[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

    val httpApp: Http[F, F] = Router[F](mappings =
      "/api/ping" -> HttpRoutes.of[F] { case GET -> Root => Ok("pong") },
      "/api/v1/comments" -> v1CommentEndpoints.httpRoutes,
      "/api/v1/tweets" -> v1TweetEndpoints.httpRoutes,
      "/api/v2" -> v2CommentEnpoints.httpRoutes,
      "/api/v2" -> v2TweetEndpoints.httpRoutes,
      "/api/v2" -> v2SwaggerDocsEndpoints.httpRoutes,
      "/api/graphql" -> graphQLEndpoint.httpRoutes
    ).orNotFound

    JettyBuilder[F]
      .bindHttp(config.port, config.host)
      .withIdleTimeout(1.minute)
      .withServiceErrorHandler(DefaultServiceErrorHandler)
      .mountService(
        HttpRoutes.of { case request => config.corsPolicy(httpApp).run(request) },
        prefix = ""
      )
  }

}

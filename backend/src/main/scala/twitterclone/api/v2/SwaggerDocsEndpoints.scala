package twitterclone.api.v2

import cats.effect.kernel.Async
import org.http4s.HttpRoutes
import sttp.apispec.openapi.OpenAPI
import sttp.tapir.AnyEndpoint
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import twitterclone.api.v2.domain.{CommentEndpoints, TweetEndpoints}

final case class SwaggerDocsEndpoints[F[_]](httpRoutes: HttpRoutes[F])

object SwaggerDocsEndpoints {

  def create[F[_]: Async]: SwaggerDocsEndpoints[F] = {

    val allEndpoints: List[AnyEndpoint] =
      CommentEndpoints.allEndpoints ++ TweetEndpoints.allTweetEndpoints

    val customiseDocsModel: OpenAPI => OpenAPI = { openApi =>
      val prefixedPathItems = openApi.paths.pathItems.map { case (key, value) =>
        ("/api/v2" + key, value)
      }
      openApi.copy(paths = openApi.paths.copy(pathItems = prefixedPathItems))
    }

    val swaggerEndpoints: List[ServerEndpoint[Any, F]] =
      SwaggerInterpreter(customiseDocsModel = customiseDocsModel)
        .fromEndpoints[F](
          endpoints = allEndpoints,
          "Twitter Clone",
          "2.0.0"
        )

    val httpRoutes: HttpRoutes[F] =
      Http4sServerInterpreter[F]().toRoutes(swaggerEndpoints)

    SwaggerDocsEndpoints(httpRoutes)
  }

}

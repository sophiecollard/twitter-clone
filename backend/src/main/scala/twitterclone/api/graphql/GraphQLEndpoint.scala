package twitterclone.api.graphql

import cats.effect.kernel.Async
import cats.implicits._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import twitterclone.api.syntax.{RequestSyntax, withNoServiceError}
import twitterclone.model.graphql.GraphQLQuery
import twitterclone.services.graphql.domain.GraphQLService

final case class GraphQLEndpoint[F[_]](httpRoutes: HttpRoutes[F])

object GraphQLEndpoint {

  def apply[F[_]: Async](service: GraphQLService[F]): GraphQLEndpoint[F] = {
    object dsl extends Http4sDsl[F]
    import dsl._

    val publicRoutes = HttpRoutes.of[F] {
      case req @ POST -> Root =>
        req.withBodyAs[GraphQLQuery] { query =>
          service.serveQuery(query).flatMap {
            withNoServiceError { json =>
              Ok(json)
            }
          }
        }
    }

    GraphQLEndpoint(publicRoutes)
  }

}

package twitterclone.services.graphql.interpreters

import cats.effect.kernel.Async
import cats.implicits._
import io.circe.Json
import sangria.execution.Executor
import sangria.schema._
import sangria.marshalling.circe._
import twitterclone.model.graphql.GraphQLQuery
import twitterclone.services.AllServices
import twitterclone.services.error.{ServiceError, ServiceErrorOr}
import twitterclone.services.error.ServiceError.graphQLInterpretationError
import twitterclone.services.graphql.domain.GraphQLService

import scala.concurrent.ExecutionContext

object SangriaGraphQLService {

  def apply[F[_]: Async](
    schema: Schema[AllServices[F], Unit],
    allServices: AllServices[F]
  )(implicit ec: ExecutionContext): GraphQLService[F] =
    new GraphQLService[F] {
      override def serveQuery(query: GraphQLQuery): F[ServiceErrorOr[Json]] =
        Async[F].fromFuture {
          Async[F].delay {
            val jsonFuture = Executor.execute(
              schema,
              queryAst = query.ast,
              userContext = allServices,
              maxQueryDepth = Some(5)
            )
            jsonFuture.map(_.asRight[ServiceError]).recover[ServiceErrorOr[Json]] { case throwable =>
              Left(graphQLInterpretationError(throwable.getMessage))
            }
          }
        }
    }

}

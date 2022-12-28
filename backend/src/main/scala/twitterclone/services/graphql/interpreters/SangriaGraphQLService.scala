package twitterclone.services.graphql.interpreters

import cats.effect.kernel.Async
import cats.implicits._
import io.circe.Json
import sangria.execution.Executor
import sangria.execution.deferred.DeferredResolver
import sangria.schema._
import sangria.marshalling.circe._
import twitterclone.model.graphql.GraphQLQuery
import twitterclone.repositories.domain.AllRepositories
import twitterclone.services.error.{ServiceError, ServiceErrorOr}
import twitterclone.services.error.ServiceError.graphQLInterpretationError
import twitterclone.services.graphql.domain.GraphQLService

import scala.concurrent.ExecutionContext

object SangriaGraphQLService {

  def apply[F[_]: Async](
    schema: Schema[AllRepositories[F], Unit],
    repositories: AllRepositories[F],
    deferredResolver: DeferredResolver[AllRepositories[F]]
  )(implicit ec: ExecutionContext): GraphQLService[F] =
    new GraphQLService[F] {
      override def serveQuery(query: GraphQLQuery): F[ServiceErrorOr[Json]] =
        Async[F].fromFuture {
          Async[F].delay {
            val jsonFuture = Executor.execute(
              schema,
              queryAst = query.ast,
              userContext = repositories,
              deferredResolver = deferredResolver,
              maxQueryDepth = Some(5)
            )
            jsonFuture.map(_.asRight[ServiceError]).recover[ServiceErrorOr[Json]] { case throwable =>
              Left(graphQLInterpretationError(throwable.getMessage))
            }
          }
        }
    }

}

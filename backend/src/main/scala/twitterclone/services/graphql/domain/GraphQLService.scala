package twitterclone.services.graphql.domain

import io.circe.Json
import twitterclone.model.graphql.GraphQLQuery
import twitterclone.services.error.ServiceErrorOr

trait GraphQLService[F[_]] {

  def serveQuery(query: GraphQLQuery): F[ServiceErrorOr[Json]]

}

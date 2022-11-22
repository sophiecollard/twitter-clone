package twitterclone.api.v2.domain

import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe.jsonBody
import instances.commentIdCodec
import twitterclone.api.error.ApiError
import twitterclone.model.{Comment, Id}

object CommentEndpoints {

  lazy val getCommentEndpoint: PublicEndpoint[Id[Comment], ApiError, Comment, Any] =
    endpoint.get
      .in("comments" / path[Id[Comment]])
      .out(jsonBody[Comment])
      .errorOut(jsonBody[ApiError])
      .description("Fetch a comment by its id")

  lazy val allEndpoints: List[AnyEndpoint] =
    List(getCommentEndpoint).map(_.tag("Comments"))

}

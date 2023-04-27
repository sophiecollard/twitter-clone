package twitterclone.api.v2.domain

import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe.jsonBody
import twitterclone.api.error.ApiError
import twitterclone.api.model.PostCommentRequest
import twitterclone.model.user.User
import twitterclone.model.{Comment, Id, Pagination, Tweet}

object CommentApiEndpoints {

  lazy val postCommentEndpoint: Endpoint[Id[User], PostCommentRequest, ApiError, Comment, Any] =
    endpoint.post
      .securityIn(header[Id[User]]("x-user-id"))
      .in("comments")
      .in(jsonBody[PostCommentRequest])
      .out(jsonBody[Comment] and statusCode(StatusCode.Created))
      .errorOut(jsonBody[ApiError])
      .description("Post a new comment")

  lazy val deleteCommentEndpoint: Endpoint[Id[User], Id[Comment], ApiError, Unit, Any] =
    endpoint.delete
      .securityIn(header[Id[User]]("x-user-id"))
      .in("comments" / path[Id[Comment]]("commentId"))
      .errorOut(jsonBody[ApiError])
      .description("Delete the comment with the specified ID")

  lazy val getCommentEndpoint: PublicEndpoint[Id[Comment], ApiError, Comment, Any] =
    endpoint.get
      .in("comments" / path[Id[Comment]]("commentId"))
      .out(jsonBody[Comment])
      .errorOut(jsonBody[ApiError])
      .description("Fetch the comment with the specified ID")

  lazy val listCommentsEndpoint: PublicEndpoint[(Id[Tweet], Pagination), ApiError, List[Comment], Any] =
    endpoint.get
      .in("comments")
      .in(query[Id[Tweet]]("tweetId") and paginationInput)
      .out(jsonBody[List[Comment]])
      .errorOut(jsonBody[ApiError])
      .description("Fetch a list of the latest comments for a given tweet")

  lazy val allEndpoints: List[AnyEndpoint] =
    List(postCommentEndpoint, deleteCommentEndpoint, getCommentEndpoint, listCommentsEndpoint)
      .map(_.tag("Comments"))

}

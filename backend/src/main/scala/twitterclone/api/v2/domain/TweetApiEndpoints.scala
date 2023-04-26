package twitterclone.api.v2.domain

import instances.tweetIdCodec
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe.jsonBody
import twitterclone.api.error.ApiError
import twitterclone.api.model.PostTweetRequest
import twitterclone.model.user.User
import twitterclone.model.{Id, Pagination, Tweet}

import java.time.LocalDateTime

object TweetApiEndpoints {

  lazy val postTweetEndpoint: Endpoint[Id[User], PostTweetRequest, ApiError, Tweet, Any] =
    endpoint.post
      .securityIn(header[Id[User]]("x-user-id"))
      .in("tweets")
      .in(jsonBody[PostTweetRequest])
      .out(jsonBody[Tweet] and statusCode(StatusCode.Created))
      .errorOut(jsonBody[ApiError])
      .description("Post a new tweet")

  lazy val deleteTweetEndpoint: Endpoint[Id[User], Id[Tweet], ApiError, Unit, Any] =
    endpoint.delete
      .securityIn(header[Id[User]]("x-user-id"))
      .in("tweets" / path[Id[Tweet]]("tweetId"))
      .errorOut(jsonBody[ApiError])
      .description("Delete the tweet with the specified ID")

  lazy val getTweetEndpoint: PublicEndpoint[Id[Tweet], ApiError, Tweet, Any] =
    endpoint.get
      .in("tweets" / path[Id[Tweet]]("tweetId"))
      .out(jsonBody[Tweet])
      .errorOut(jsonBody[ApiError])
      .description("Fetch the tweet with the specified ID")

  lazy val paginationInput: EndpointInput[Pagination] =
    (query[Option[Int]]("pageSize") and query[Option[LocalDateTime]]("postedBefore")).map(
      Mapping.from[(Option[Int], Option[LocalDateTime]), Pagination] {
        case (maybePage, postedBefore) =>
          Pagination(pageSize = maybePage.getOrElse(10), postedBefore)
      } (pagination => (Some(pagination.pageSize), pagination.postedBefore))
    )

  lazy val listTweetsEndpoint: PublicEndpoint[(Option[Id[User]], Pagination), ApiError, List[Tweet], Any] =
    endpoint.get
      .in("tweets")
      .in(query[Option[Id[User]]]("authorId") and paginationInput)
      .out(jsonBody[List[Tweet]])
      .errorOut(jsonBody[ApiError])
      .description("Fetch a list of the latest tweets")

  lazy val allTweetEndpoints: List[AnyEndpoint] =
    List(postTweetEndpoint, deleteTweetEndpoint, getTweetEndpoint, listTweetsEndpoint)
      .map(_.tag("Tweets"))

}

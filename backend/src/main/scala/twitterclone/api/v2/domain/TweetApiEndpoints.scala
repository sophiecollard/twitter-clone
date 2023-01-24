package twitterclone.api.v2.domain

import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe.jsonBody
import instances.tweetIdCodec
import twitterclone.api.error.ApiError
import twitterclone.model.{Id, Tweet}

object TweetApiEndpoints {

  lazy val getTweetEndpoint: PublicEndpoint[Id[Tweet], ApiError, Tweet, Any] =
    endpoint.get
      .in("tweets" / path[Id[Tweet]])
      .out(jsonBody[Tweet])
      .errorOut(jsonBody[ApiError])
      .description("Fetch a tweet by its id")

  lazy val allTweetEndpoints: List[AnyEndpoint] =
    List(getTweetEndpoint).map(_.tag("Tweets"))

}

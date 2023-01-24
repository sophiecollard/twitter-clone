package twitterclone.api.v2.interpreters

import cats.effect.kernel.Async
import cats.implicits._
import org.http4s.HttpRoutes
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter
import twitterclone.api.error.ApiError
import twitterclone.api.v2.domain.TweetApiEndpoints
import twitterclone.services.tweet.TweetService

final case class Http4sTweetApiEndpoints[F[_]](httpRoutes: HttpRoutes[F])

object Http4sTweetApiEndpoints {

  def apply[F[_] : Async](tweetService: TweetService[F]): Http4sTweetApiEndpoints[F] = {

    val getTweetEndpoint: ServerEndpoint[Any, F] =
      TweetApiEndpoints
        .getTweetEndpoint
        .serverLogic(id => tweetService.get(id).map(_.leftMap(ApiError.fromServiceError)))

    val publicRoutes: HttpRoutes[F] =
      Http4sServerInterpreter[F]().toRoutes(getTweetEndpoint :: Nil)

    Http4sTweetApiEndpoints(publicRoutes)
  }

}

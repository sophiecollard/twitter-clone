package twitterclone.api.v2.interpreters

import cats.effect.kernel.Async
import cats.implicits._
import org.http4s.HttpRoutes
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter
import twitterclone.api.error.ApiError
import twitterclone.api.v2.domain.TweetApiEndpoints
import twitterclone.auth.{Failure, Success}
import twitterclone.services.tweet.TweetService

final case class Http4sTweetApiEndpoints[F[_]](httpRoutes: HttpRoutes[F])

object Http4sTweetApiEndpoints {

  def apply[F[_] : Async](tweetService: TweetService[F]): Http4sTweetApiEndpoints[F] = {

    val postTweetEndpoint: ServerEndpoint[Any, F] =
      TweetApiEndpoints
        .postTweetEndpoint
        .serverSecurityLogicPure(_.asRight)
        .serverLogic { userId => request =>
          tweetService.create(request.contents)(userId).map(_.leftMap(ApiError.fromServiceError))
        }

    val deleteTweetEndpoint: ServerEndpoint[Any, F] =
      TweetApiEndpoints
        .deleteTweetEndpoint
        .serverSecurityLogicPure(_.asRight)
        .serverLogic { userId => id =>
          tweetService.delete(id)(userId).map {
            case Success(result) => result.leftMap(ApiError.fromServiceError)
            case Failure(error)  => Left(ApiError.fromAuthorizationError(error))
          }
        }

    val getTweetEndpoint: ServerEndpoint[Any, F] =
      TweetApiEndpoints
        .getTweetEndpoint
        .serverLogic(id => tweetService.get(id).map(_.leftMap(ApiError.fromServiceError)))

    val listTweetsEndpoint: ServerEndpoint[Any, F] =
      TweetApiEndpoints
        .listTweetsEndpoint
        .serverLogic {
          case (Some(authorId), pagination) =>
            tweetService.listBy(authorId, pagination).map(_.leftMap(ApiError.fromServiceError))
          case (None, pagination) =>
            tweetService.list(pagination).map(_.leftMap(ApiError.fromServiceError))
        }

    val publicRoutes: HttpRoutes[F] =
      Http4sServerInterpreter[F]()
        .toRoutes(postTweetEndpoint :: deleteTweetEndpoint :: getTweetEndpoint :: listTweetsEndpoint :: Nil)

    Http4sTweetApiEndpoints(publicRoutes)
  }

}

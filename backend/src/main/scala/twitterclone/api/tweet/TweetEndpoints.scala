package twitterclone.api.tweet

import cats.effect.Concurrent
import cats.implicits._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.AuthMiddleware
import org.http4s.{AuthedRoutes, HttpRoutes}
import twitterclone.api.shared.extractors.TweetIdVar
import twitterclone.api.shared.matchers._
import twitterclone.api.syntax._
import twitterclone.api.tweet.instances._
import twitterclone.model.{Id, TweetPagination, User}
import twitterclone.services.tweet.TweetService

final case class TweetEndpoints[F[_]](httpRoutes: HttpRoutes[F])

object TweetEndpoints {

  def create[F[_]: Concurrent](
    authMiddleware: AuthMiddleware[F, Id[User]],
    service: TweetService[F]
  ): TweetEndpoints[F] = {
    object dsl extends Http4sDsl[F]
    import dsl._

    val publicRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
      case GET -> Root / TweetIdVar(id) =>
        service.get(id).flatMap {
          withNoServiceError { tweet =>
            Ok(tweet)
          }
        }
      case GET -> Root :?
        AuthorQueryParamMatcher(author) +&
          PageSizeOptionalQueryParamMatcher(pageSize) +&
          PostedBeforeOptionalQueryParamMatcher(postedBefore) =>
        service.listBy(author, TweetPagination(pageSize.getOrElse(10), postedBefore)).flatMap {
          withNoServiceError { tweets =>
            Ok(tweets)
          }
        }
    }

    val privateRoutes: AuthedRoutes[Id[User], F] = AuthedRoutes.of[Id[User], F] {
      case authedRequest @ POST -> Root as userId =>
        authedRequest.req.withBodyAs[NewTweetRequestBody] { requestBody =>
          service.create(requestBody.contents)(userId).flatMap {
            withNoServiceError { tweet =>
              Created(tweet)
            }
          }
        }
      case DELETE -> Root / TweetIdVar(id) as userId =>
        service.delete(id)(userId).flatMap {
          withSuccessfulAuthorization {
            withNoServiceError { _ =>
              Ok()
            }
          }
        }
    }

    TweetEndpoints(publicRoutes <+> authMiddleware(privateRoutes))
  }

}

package twitterclone.api.v1.tweet

import cats.effect.Concurrent
import cats.implicits._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.AuthMiddleware
import org.http4s.{AuthedRoutes, HttpRoutes}
import twitterclone.api.syntax._
import twitterclone.api.v1.shared.extractors.TweetIdVar
import twitterclone.api.v1.shared.matchers.{AuthorQueryParamMatcher, PageSizeOptionalQueryParamMatcher, PostedBeforeOptionalQueryParamMatcher}
import twitterclone.model.user.User
import twitterclone.model.{Id, TweetPagination}
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
      case GET -> Root :?
        PageSizeOptionalQueryParamMatcher(pageSize) +&
          PostedBeforeOptionalQueryParamMatcher(postedBefore) =>
        service.list(TweetPagination(pageSize.getOrElse(10), postedBefore = postedBefore)).flatMap {
          withNoServiceError { tweets =>
            Ok(tweets)
          }
        }
    }

    val privateRoutes: AuthedRoutes[Id[User], F] = AuthedRoutes.of[Id[User], F] {
      case authedRequest @ POST -> Root as userId =>
        authedRequest.req.withBodyAs[PostTweetRequest] { requestBody =>
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

package twitterclone.api.v1.comment

import cats.effect.Concurrent
import cats.implicits._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.AuthMiddleware
import org.http4s.{AuthedRoutes, HttpRoutes}
import twitterclone.api.syntax._
import twitterclone.api.v1.shared.extractors.CommentIdVar
import twitterclone.api.v1.shared.matchers.{PageSizeOptionalQueryParamMatcher, PostedBeforeOptionalQueryParamMatcher, TweetIdQueryParamMatcher}
import twitterclone.model.user.User
import twitterclone.model.{CommentPagination, Id}
import twitterclone.services.comment.CommentService

final case class CommentEndpoints[F[_]](httpRoutes: HttpRoutes[F])

object CommentEndpoints {

  def create[F[_]: Concurrent](
    authMiddleware: AuthMiddleware[F, Id[User]],
    service: CommentService[F]
  ): CommentEndpoints[F] = {
    object dsl extends Http4sDsl[F]
    import dsl._

    val publicRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
      case GET-> Root / CommentIdVar(id) =>
          service.get(id).flatMap{
            withNoServiceError { comment =>
              Ok(comment)
            }
          }
      case GET -> Root :?
        TweetIdQueryParamMatcher(tweetId) +&
          PageSizeOptionalQueryParamMatcher(pageSize) +&
          PostedBeforeOptionalQueryParamMatcher(postedBefore) =>
            service.list(tweetId, CommentPagination(pageSize.getOrElse(10), postedBefore)).flatMap {
              withNoServiceError { comments =>
                Ok(comments)
              }
            }
    }

    val privateRoutes: AuthedRoutes[Id[User], F] = AuthedRoutes.of[Id[User], F] {
      case authedRequest @ POST -> Root as userId =>
        authedRequest.req.withBodyAs[PostCommentRequest] { requestBody =>
          service.create(requestBody.tweetId, requestBody.contents)(userId).flatMap {
            withNoServiceError { comment =>
              Created(comment)
            }
          }
        }
      case DELETE -> Root / CommentIdVar(id) as userId =>
        service.delete(id)(userId).flatMap {
          withSuccessfulAuthorization {
            withNoServiceError { _ =>
              Ok()
            }
          }
        }
    }

    CommentEndpoints(publicRoutes <+> authMiddleware(privateRoutes))
  }

}

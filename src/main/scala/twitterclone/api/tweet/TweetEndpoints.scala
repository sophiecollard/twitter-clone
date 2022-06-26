package twitterclone.api.tweet

import cats.effect.Async
import cats.implicits._
import org.http4s.dsl.Http4sDsl
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.impl.{OptionalQueryParamDecoderMatcher, QueryParamDecoderMatcher}
import org.http4s.server.AuthMiddleware
import org.http4s.{AuthedRoutes, HttpRoutes, QueryParamDecoder}
import twitterclone.api.syntax._
import twitterclone.api.tweet.instances._
import twitterclone.api.tweet.model.NewTweetRequestBody
import twitterclone.model.{Id, Tweet, TweetPagination, User}
import twitterclone.services.tweet.TweetService

import java.time.format.DateTimeFormatter
import java.time.ZonedDateTime
import java.util.UUID
import scala.util.Try

object TweetEndpoints {

  def create[F[_]: Async](
    authMiddleware: AuthMiddleware[F, Id[User]],
    service: TweetService[F]
  ): HttpRoutes[F] = {
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
          PageSizeQueryQueryParamMatcher(pageSize) +&
          PostedAfterOptionalQueryParamMatcher(postedAfter) =>
        service.list(author, TweetPagination(pageSize, postedAfter)).flatMap {
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

    publicRoutes <+> authMiddleware(privateRoutes)
  }

  // TODO move all this stuff

  object TweetIdVar {
    def unapply(value: String): Option[Id[Tweet]] =
      Try(UUID.fromString(value))
        .map(Id.apply[Tweet])
        .toOption
  }

  implicit val uuidQueryParamDecoder: QueryParamDecoder[UUID] =
    QueryParamDecoder[String].map(UUID.fromString)

  implicit def idQueryParamDecoder[A]: QueryParamDecoder[Id[A]] =
    QueryParamDecoder[UUID].map(Id.apply[A])

  implicit val zonedDateTimeParamDecoder: QueryParamDecoder[ZonedDateTime] =
    QueryParamDecoder[String].map(ZonedDateTime.parse(_, DateTimeFormatter.ISO_ZONED_DATE_TIME))

  object AuthorQueryParamMatcher
    extends QueryParamDecoderMatcher[Id[User]]("author")

  object PageSizeQueryQueryParamMatcher
    extends QueryParamDecoderMatcher[Int]("page_size")

  object PostedAfterOptionalQueryParamMatcher
    extends OptionalQueryParamDecoderMatcher[ZonedDateTime]("posted_after")

}

package twitterclone.services.tweet

import cats.{Monad, ~>}
import cats.implicits._
import twitterclone.auth.AuthorizationService
import twitterclone.model.user.User
import twitterclone.model.{Id, Tweet, TweetPagination}
import twitterclone.repositories.domain.TweetRepository
import twitterclone.services.analytics.AnalyticsService
import twitterclone.services.analytics.AnalyticsService.Registerable
import twitterclone.services.analytics.publishing.{FilePublisher}
import twitterclone.services.error.ServiceError.{failedToCreateResource, failedToDeleteResource, resourceNotFound}
import twitterclone.services.error.{ServiceError, ServiceErrorOr}
import twitterclone.services.syntax._
import twitterclone.services.tweet.auth.{ByAuthor, WithAuthorizationByAuthor}

import java.time.{LocalDateTime, ZoneId}

trait TweetService[F[_]] {

  /** Creates a new tweet */
  def create(contents: String)(userId: Id[User]): F[ServiceErrorOr[Tweet]]

  /** Deletes a tweet */
  def delete(id: Id[Tweet])(userId: Id[User]): F[WithAuthorizationByAuthor[ServiceErrorOr[Unit]]]

  /** Fetches a tweet */
  def get(id: Id[Tweet]): F[ServiceErrorOr[Tweet]]

  /** Fetches tweets from any author */
  def list(pagination: TweetPagination = TweetPagination.default): F[ServiceErrorOr[List[Tweet]]]

  /** Fetches tweets from a given author */
  def listBy(author: Id[User], pagination: TweetPagination = TweetPagination.default): F[ServiceErrorOr[List[Tweet]]]

}

object TweetService {

  def create[F[_], G[_]: Monad](
    tweetRepository: TweetRepository[G],
    authByAuthorService: AuthorizationService[G, (Id[User], Id[Tweet]), ByAuthor]
  )(implicit transactor: G ~> F): TweetService[F] =
    new TweetService[F] {
      /** Creates a new tweet */

        val analyticsService = new AnalyticsService(FilePublisher)

      case class UserWithIdTweeted(userId: Id[User], tweet: Tweet)

      implicit val registerableUserWithIdTweeted: Registerable[UserWithIdTweeted] =
        new Registerable[UserWithIdTweeted]{
        override def registerableInformation(event: UserWithIdTweeted): String =
          s"User @${event.userId} tweeted ${event.tweet}"
      }

      override def create(contents: String)(userId: Id[User]): F[ServiceErrorOr[Tweet]] = {
        val tweet = Tweet(
          id = Id.random[Tweet],
          author = userId,
          contents,
          postedOn = LocalDateTime.now(ZoneId.of("UTC"))
        )
        tweetRepository.create(tweet).map {
          case 1 =>
            val event = UserWithIdTweeted(userId, tweet)
            analyticsService.registerMeaningfulInfo(event)
            Right(tweet)
          case _ => Left(failedToCreateResource("Tweet"))
        }.transact
      }

      /** Deletes a tweet */
      override def delete(id: Id[Tweet])(userId: Id[User]): F[WithAuthorizationByAuthor[ServiceErrorOr[Unit]]] =
        authByAuthorService.authorize((userId, id)) {
          tweetRepository.delete(id).map {
            case 1 => Right(())
            case _ => Left(failedToDeleteResource(id, "Tweet"))
          }
        }.transact

      /** Fetches a tweet */
      override def get(id: Id[Tweet]): F[ServiceErrorOr[Tweet]] =
        tweetRepository.get(id).map {
          case Some(tweet) => Right(tweet)
          case None        => Left(resourceNotFound(id, "Tweet"))
        }.transact

      /** Fetches tweets from any author */
      override def list(pagination: TweetPagination = TweetPagination.default): F[ServiceErrorOr[List[Tweet]]] =
        tweetRepository
          .list(pagination)
          .map(_.asRight[ServiceError])
          .transact

      /** Fetches tweets from a given author */
      override def listBy(author: Id[User], pagination: TweetPagination = TweetPagination.default): F[ServiceErrorOr[List[Tweet]]] =
        tweetRepository
          .listBy(author, pagination)
          .map(_.asRight[ServiceError])
          .transact
    }

}

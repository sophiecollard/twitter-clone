package twitterclone.services.tweet

import cats.{Monad, ~>}
import cats.implicits._
import twitterclone.auth.AuthorizationService
import twitterclone.model.{Id, Tweet, TweetPagination, User}
import twitterclone.repositories.tweet.TweetRepository
import twitterclone.services.error.ServiceError.{failedToCreateResource, failedToDeleteResource, resourceNotFound}
import twitterclone.services.error.{ServiceError, ServiceErrorOr}
import twitterclone.services.syntax._
import twitterclone.services.tweet.auth.{ByAuthor, WithAuthorizationByAuthor}

import java.time.{LocalDateTime, ZoneId}

trait TweetService[F[_]] {

  /** Creates a new Tweet */
  def create(contents: String)(userId: Id[User]): F[ServiceErrorOr[Tweet]]

  /** Deletes a Tweet */
  def delete(id: Id[Tweet])(userId: Id[User]): F[WithAuthorizationByAuthor[ServiceErrorOr[Unit]]]

  /** Fetches a Tweet */
  def get(id: Id[Tweet]): F[ServiceErrorOr[Tweet]]

  /** Fetches tweets from a given User */
  def list(author: Id[User], pagination: TweetPagination): F[ServiceErrorOr[List[Tweet]]]

}

object TweetService {

  def create[F[_], G[_]: Monad](
    tweetRepository: TweetRepository[G],
    authByAuthorIdService: AuthorizationService[G, (Id[User], Id[Tweet]), ByAuthor]
  )(implicit transactor: G ~> F): TweetService[F] =
    new TweetService[F] {
      /** Creates a new Tweet */
      override def create(contents: String)(userId: Id[User]): F[ServiceErrorOr[Tweet]] = {
        val tweet = Tweet(
          id = Id.random[Tweet],
          author = userId,
          contents,
          postedOn = LocalDateTime.now(ZoneId.of("UTC"))
        )
        tweetRepository.create(tweet).map {
          case 1 => Right(tweet)
          case _ => Left(failedToCreateResource("Tweet"))
        }.transact
      }

      /** Deletes a Tweet */
      override def delete(id: Id[Tweet])(userId: Id[User]): F[WithAuthorizationByAuthor[ServiceErrorOr[Unit]]] =
        authByAuthorIdService.authorize((userId, id)) {
          tweetRepository.delete(id).map {
            case 1 => Right(())
            case _ => Left(failedToDeleteResource(id, "Tweet"))
          }
        }.transact

      /** Fetches a Tweet */
      override def get(id: Id[Tweet]): F[ServiceErrorOr[Tweet]] =
        tweetRepository.get(id).map {
          case Some(tweet) => Right(tweet)
          case None        => Left(resourceNotFound(id, "Tweet"))
        }.transact

      /** Fetches tweets from a given User */
      override def list(author: Id[User], pagination: TweetPagination): F[ServiceErrorOr[List[Tweet]]] =
        tweetRepository
          .list(author, pagination)
          .map(_.asRight[ServiceError])
          .transact
    }

}

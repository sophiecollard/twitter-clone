package twitterclone.services.tweet

import cats.{Monad, Parallel, ~>}
import cats.implicits._
import eu.timepit.refined.auto._
import twitterclone.auth.AuthorizationService
import twitterclone.model.user.User
import twitterclone.model.{Id, Pagination, Tweet}
import twitterclone.repositories.domain.{LikeRepository, TweetRepository}
import twitterclone.repositories.domain.TweetRepository.TweetData
import twitterclone.services.error.ServiceError.{failedToCreateResource, failedToDeleteResource, resourceNotFound}
import twitterclone.services.error.ServiceErrorOr
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
  def list(pagination: Pagination = Pagination.default): F[List[Tweet]]

  /** Fetches tweets from a given author */
  def listBy(authorId: Id[User], pagination: Pagination = Pagination.default): F[List[Tweet]]

}

object TweetService {

  def create[F[_]: Monad: Parallel, G[_]: Monad](
    tweetRepository: TweetRepository[G],
    likeRepository: LikeRepository[G],
    authByAuthorService: AuthorizationService[G, (Id[User], Id[Tweet]), ByAuthor]
  )(implicit transactor: G ~> F): TweetService[F] =
    new TweetService[F] {
      /** Creates a new tweet */
      override def create(contents: String)(userId: Id[User]): F[ServiceErrorOr[Tweet]] = {
        val tweet = TweetData(
          id = Id.random[Tweet],
          authorId = userId,
          contents,
          postedOn = LocalDateTime.now(ZoneId.of("UTC"))
        )
        tweetRepository.create(tweet).map {
          case 1 => Right(tweet.constructTweet(likeCount = 0))
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
      override def get(id: Id[Tweet]): F[ServiceErrorOr[Tweet]] = {
        (tweetRepository.get(id).transact, likeRepository.getLikeCount(id).transact).parMapN {
          case (Some(tweet), likeCount) => Right(tweet.constructTweet(likeCount))
          case (None, _) => Left(resourceNotFound(id, "Tweet"))
        }
      }

      /** Fetches tweets from any author */
      override def list(pagination: Pagination = Pagination.default): F[List[Tweet]] =
        tweetRepository
          .list(pagination)
          .transact
          .flatMap(_.parTraverse(enrichTweetWithLikeCount))

      /** Fetches tweets from a given author */
      override def listBy(authorId: Id[User], pagination: Pagination = Pagination.default): F[List[Tweet]] =
        tweetRepository
          .listBy(authorId, pagination)
          .transact
          .flatMap(_.parTraverse(enrichTweetWithLikeCount))

      private def enrichTweetWithLikeCount(tweetData: TweetData): F[Tweet] =
        likeRepository
          .getLikeCount(tweetData.id)
          .map(tweetData.constructTweet)
          .transact
    }

}

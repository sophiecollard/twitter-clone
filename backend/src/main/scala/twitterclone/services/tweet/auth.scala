package twitterclone.services.tweet

import cats.Monad
import cats.implicits._
import twitterclone.auth.error.AuthorizationError.NotTheTweetsAuthor
import twitterclone.auth.{AuthorizationService, WithAuthorization}
import twitterclone.model.user.User
import twitterclone.model.{Id, Tweet}
import twitterclone.repositories.tweet.TweetRepository

object auth {

  trait ByAuthor
  type WithAuthorizationByAuthor[R] = WithAuthorization[R, ByAuthor]

  def byAuthor[F[_]: Monad](
    tweetRepository: TweetRepository[F]
  ): AuthorizationService[F, (Id[User], Id[Tweet]), ByAuthor] =
    AuthorizationService.create { case (userId, tweetId) =>
      tweetRepository.getAuthor(tweetId).map {
        case Some(author) if author == userId =>
          Right(())
        case _ =>
          Left(NotTheTweetsAuthor(userId, tweetId))
      }
    }

}

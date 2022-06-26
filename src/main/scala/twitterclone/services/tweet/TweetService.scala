package twitterclone.services.tweet

import twitterclone.model.{Id, Tweet, TweetPagination, User}
import twitterclone.services.error.ServiceErrorOr
import twitterclone.services.tweet.auth.WithAuthorizationByAuthor

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

package twitterclone.repositories.tweet

import twitterclone.model.{Id, Tweet, TweetPagination, User}

trait TweetRepository[F[_]] {

  def create(tweet: Tweet): F[Int]

  def delete(id: Id[Tweet]): F[Int]

  def get(id: Id[Tweet]): F[Option[Tweet]]

  def getAuthor(id: Id[Tweet]): F[Option[Id[User]]]

  def list(pagination: TweetPagination): F[List[Tweet]]

  def listBy(author: Id[User], pagination: TweetPagination): F[List[Tweet]]

}

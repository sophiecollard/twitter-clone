package twitterclone.repositories.domain

import cats.~>
import eu.timepit.refined.types.numeric.NonNegInt
import twitterclone.model.user.User
import twitterclone.model.{Id, Pagination, Tweet}
import twitterclone.services.syntax.Transactable

import java.time.LocalDateTime

trait TweetRepository[F[_]] {
  import TweetRepository._

  def create(tweet: TweetData): F[Int]

  def delete(id: Id[Tweet]): F[Int]

  def get(id: Id[Tweet]): F[Option[TweetData]]

  def getAuthorId(id: Id[Tweet]): F[Option[Id[User]]]

  def list(pagination: Pagination): F[List[TweetData]]

  def listBy(authorId: Id[User], pagination: Pagination): F[List[TweetData]]

}

object TweetRepository {

  def mapF[F[_], G[_]](repo: TweetRepository[F])(implicit transactor: F ~> G): TweetRepository[G] =
    new TweetRepository[G] {
      override def create(tweet: TweetData): G[Int] =
        repo.create(tweet).transact

      override def delete(id: Id[Tweet]): G[Int] =
        repo.delete(id).transact

      override def get(id: Id[Tweet]): G[Option[TweetData]] =
        repo.get(id).transact

      override def getAuthorId(id: Id[Tweet]): G[Option[Id[User]]] =
        repo.getAuthorId(id).transact

      override def list(pagination: Pagination): G[List[TweetData]] =
        repo.list(pagination).transact

      override def listBy(authorId: Id[User], pagination: Pagination): G[List[TweetData]] =
        repo.listBy(authorId, pagination).transact
    }

  final case class TweetData(
    id: Id[Tweet],
    authorId: Id[User],
    contents: String,
    postedOn: LocalDateTime,
  ){
    def constructTweet(likeCount: NonNegInt, didUserLike: Option[Boolean]): Tweet =
      Tweet(id, authorId, contents, postedOn, likeCount, didUserLike)
  }

}

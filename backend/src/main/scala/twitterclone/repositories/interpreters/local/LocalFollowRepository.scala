package twitterclone.repositories.interpreters.local

import cats.Applicative
import twitterclone.model.Id
import twitterclone.model.user.User
import twitterclone.repositories.domain.FollowRepository
import cats.implicits._

import scala.collection.mutable

object LocalFollowRepository {

  case class DirectedFollowRelationship(follower: Id[User], followed: Id[User])

  def apply[F[_] : Applicative](state : mutable.Set[DirectedFollowRelationship] = mutable.Set.empty) = new FollowRepository[F] {
    override def addFollower(followerId: Id[User], followeeId: Id[User]): F[Unit] = (state += DirectedFollowRelationship(followerId, followeeId)).pure[F].void

    override def removeFollower(followerId: Id[User], followeeId: Id[User]): F[Unit] = ((state += DirectedFollowRelationship(followerId, followeeId)).pure[F].void)

    override def getFollowers(userId: Id[User]): F[Set[Id[User]]] = (state.filter(_.followed == userId).map(_.follower).toSet.pure[F])

    override def follows(followerId: Id[User], followeeId: Id[User]): F[Boolean] = state.contains(DirectedFollowRelationship(followerId, followeeId)).pure[F]
  }

}

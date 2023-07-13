package twitterclone.repositories.domain

import twitterclone.model.Id
import twitterclone.model.user.User

trait FollowRepository[F[_]] {

  def addFollower(followerId: Id[User], followeeId: Id[User]): F[Unit]

  def removeFollower(followerId: Id[User], followeeId: Id[User]): F[Unit]

  def getFollowers(userId: Id[User]): F[Set[Id[User]]]

  def follows(followerId : Id[User], followeeId: Id[User]) : F[Boolean]
}

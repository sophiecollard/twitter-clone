package twitterclone.model.graphql

import sangria.execution.deferred.Deferred
import twitterclone.model.user.User
import twitterclone.model.{Comment, Id, Pagination, Tweet}

sealed trait DeferredType

object DeferredType {

  final case class TweetById(id: Id[Tweet])
    extends Deferred[Option[Tweet]] with DeferredType

  final case class TweetsByUserId(userId: Id[User], pagination: Pagination)
    extends Deferred[List[Tweet]] with DeferredType

  final case class CommentsByTweetId(tweetId: Id[Tweet], pagination: Pagination)
    extends Deferred[List[Comment]] with DeferredType

  final case class CommentsByUserId(userId: Id[User], pagination: Pagination)
    extends Deferred[List[Comment]] with DeferredType

  final case class UserById(id: Id[User])
    extends Deferred[Option[User]] with DeferredType

}

package twitterclone.model.graphql

import sangria.execution.deferred.Deferred
import twitterclone.model.{Comment, CommentPagination, Id, Tweet}

sealed trait DeferredType

object DeferredType {

  final case class TweetById(id: Id[Tweet])
    extends Deferred[Option[Tweet]] with DeferredType

  final case class CommentsByTweetId(tweetId: Id[Tweet], pagination: CommentPagination)
    extends Deferred[List[Comment]] with DeferredType

}

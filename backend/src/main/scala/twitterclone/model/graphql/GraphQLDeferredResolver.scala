package twitterclone.model.graphql

import cats.effect.IO
import cats.implicits._
import cats.effect.unsafe.IORuntime
import sangria.execution.deferred.{Deferred, DeferredResolver}
import twitterclone.repositories.domain.AllRepositories

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.reflect.ClassTag
import scala.util.Success

object GraphQLDeferredResolver {

  def apply(implicit ior: IORuntime): DeferredResolver[AllRepositories[IO]] =
    new DeferredResolver[AllRepositories[IO]] {
      override def resolve(
        deferred: Vector[Deferred[Any]],
        ctx: AllRepositories[IO],
        queryState: Any
      )(implicit ec: ExecutionContext): Vector[Future[Any]] = {
        // Deduplicate deferred values and associate each one with an unfulfilled promise
        val promises: Map[Deferred[Any], Promise[Any]] =
          deferred.map(d => d -> Promise[Any]()).toMap

        // Select the distinct deferred values of the given class
        // No other choice since we're given Any
        def select[A <: Deferred[Any] : ClassTag]: List[A] =
          promises.keys.collect { case a: A => a } .toList

        // Complete the promise associated with a Deferred instance
        def completeWithSuccess[A](d: Deferred[A], a: A): IO[Unit] =
          IO.delay(promises(d).complete(Success(a))).void

        def completeTweetById(ds: List[DeferredType.TweetById]): IO[Unit] =
          for {
            tweets <- ds.traverse(d => ctx.tweets.get(d.id).map(d -> _))
            _ <- tweets.traverse { case (d, tweet) => completeWithSuccess(d, tweet) }
          } yield ()

        def completeTweetsByUserId(ds: List[DeferredType.TweetsByUserId]): IO[Unit] =
          for {
            tweets <- ds.traverse(d => ctx.tweets.listBy(d.userId, d.pagination).map(d -> _))
            _ <- tweets.traverse { case (d, tweets) => completeWithSuccess(d, tweets) }
          } yield ()

        def completeCommentsByTweetId(ds: List[DeferredType.CommentsByTweetId]): IO[Unit] =
          for {
            comments <- ds.traverse(d => ctx.comments.list(d.tweetId, d.pagination).map(d -> _))
            _ <- comments.traverse { case (d, comments) => completeWithSuccess(d, comments) }
          } yield ()

        def completeCommentsByUserId(ds: List[DeferredType.CommentsByUserId]): IO[Unit] =
          for {
            comments <- ds.traverse(d => ctx.comments.listBy(d.userId, d.pagination).map(d -> _))
            _ <- comments.traverse { case (d, comments) => completeWithSuccess(d, comments) }
          } yield ()

        def completeUserById(ds: List[DeferredType.UserById]): IO[Unit] =
          for {
            users <- ds.traverse(d => ctx.users.get(d.id).map(d -> _))
            _ <- users.traverse { case (d, user) => completeWithSuccess(d, user) }
          } yield ()

        // WARNING Don't forget to complete ALL the possible deferred values
        // Otherwise, orphaned promises will just wait forever
        completeTweetById(select[DeferredType.TweetById]).unsafeToFuture()
        completeTweetsByUserId(select[DeferredType.TweetsByUserId]).unsafeToFuture()
        completeCommentsByTweetId(select[DeferredType.CommentsByTweetId]).unsafeToFuture()
        completeCommentsByUserId(select[DeferredType.CommentsByUserId]).unsafeToFuture()
        completeUserById(select[DeferredType.UserById]).unsafeToFuture()

        deferred.map(promises(_).future)
      }
    }

}

//package twitterclone.api.endpoints
//
//import java.util.UUID
//import cats.effect.Concurrent
//import org.http4s.{AuthedRoutes, HttpRoutes}
//import org.http4s.dsl.Http4sDsl
//import org.http4s.server.AuthMiddleware
//import twitterclone.model.{Id, Tweet, User}
//import twitterclone.services.tweets.TweetsService
//
//import scala.util.Try
//
//object tweets {
//
//  def create[F[_]: Concurrent](
//    authMiddleware: AuthMiddleware[F, Id[User]],
//    service: TweetsService[F]
//  ): HttpRoutes[F] = {
//    object dsl extends Http4sDsl[F]
//    import dsl._
//
//    val privateRoutes: AuthedRoutes[, F]
//
//    val publicRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
//      case GET -> Root / "tweets" / TweetIdVar(tweetId) =>
//        ???
//    }
//
//
//    ???
//  }
//
//  object TweetIdVar {
//    def unapply(string: String): Option[Id[Tweet]] =
//      Try(UUID.fromString(string))
//        .map(Id.apply[Tweet])
//        .toOption
//  }
//
//}

package twitterclone.services.analytics

import cats.Functor
import twitterclone.model.Tweet
import twitterclone.model.user.User
import twitterclone.services.analytics.publishing.AnalyticsPublisher

class AnalyticsService[F[_]: Functor](publisher: AnalyticsPublisher[F]) {
  import AnalyticsService._


  def registerUserCreated(user: User): F[Unit] = {
    implicitly[Functor[F]].map(publisher.register(s"$user created"))({_: Any => println("Event registered")})
  }

  def registerUserTweeted(user: User, tweet: Tweet) = publisher.register(s"$user tweeted $tweet")

  sealed trait Event
  case object ServerStarted extends Event
  case class UserCreated(user: User) extends Event
  case class UserTweeted(user: User, tweet: Tweet) extends Event

  def registerEvent(event: Event) = event match {
    case ServerStarted => publisher.register(s"server started")
    case UserCreated(user) => publisher.register(s"$user created")
    case UserTweeted(user, tweet) => publisher.register(s"$user tweeted $tweet")
    case _ => ???
  }

  def registerMeaningfulInfo[MeaningfulInfo: Registerable](info: MeaningfulInfo) = {
    val re = implicitly[Registerable[MeaningfulInfo]]
    val message = re.registerableInformation(info)
    publisher.register(message)
  }

}

object AnalyticsService{

  trait Registerable[A]{
    def registerableInformation(a: A): String
  }

}

package twitterclone.services.analytics.publishing
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object InternetPublisher extends AnalyticsPublisher[Future] {
  override def register(info: String): Future[Unit] = Future(regiterToTheInternet(info))

  private def regiterToTheInternet(info: String) = {
    println(info)
    Thread.sleep(1000)
  }
}

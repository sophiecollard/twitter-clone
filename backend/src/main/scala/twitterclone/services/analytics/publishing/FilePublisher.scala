package twitterclone.services.analytics.publishing

import scala.util.Try

object FilePublisher extends AnalyticsPublisher[Try] {
  override def register(info: String): Try[Unit] = writeToFile(info)


  private def writeToFile(str: String): Try[Unit] = Try(println(str))
}

package twitterclone.services.analytics.publishing


trait AnalyticsPublisher[F[_]] {

  def register(info: String): F[Unit]

}

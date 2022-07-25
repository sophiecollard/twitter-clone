package twitterclone.config

import ciris.{ConfigValue, Effect, env}
import org.http4s.server.middleware.CORSConfig
import twitterclone.config.instances._

import scala.annotation.nowarn

@nowarn("cat=deprecation")
final case class ServerConfig(
  host: String,
  port: Int,
  cors: CORSConfig
)

object ServerConfig {

  @nowarn("cat=deprecation")
  val configValue: ConfigValue[Effect, ServerConfig] =
    for {
      host <- env("SERVER_HOST").as[String]
      port <- env("SERVER_PORT").as[Int]
      allowedOrigins <- env("ALLOWED_ORIGINS").as[Set[String]]
      cors = CORSConfig.default.withAnyOrigin(false).withAllowedOrigins(allowedOrigins)
    } yield ServerConfig(host, port, cors)

}

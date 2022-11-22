package twitterclone.config

import ciris.{ConfigValue, Effect, env}
import org.http4s.headers.Origin
import org.http4s.server.middleware.{CORS, CORSPolicy}
import twitterclone.config.instances._

final case class ServerConfig(
  host: String,
  port: Int,
  corsPolicy: CORSPolicy
)

object ServerConfig {

  val configValue: ConfigValue[Effect, ServerConfig] =
    for {
      host <- env("SERVER_HOST").as[String].default("0.0.0.0")
      port <- env("SERVER_PORT").as[Int].default(8080)
      allowedOrigins <- env("ALLOWED_ORIGINS").as[Set[String]].default(Set("localhost"))
      isAllowedOriginHost = (originHost: Origin.Host) => allowedOrigins.exists(originHost.host.value.endsWith)
      corsPolicy = CORS.policy.withAllowOriginHost(isAllowedOriginHost)
    } yield ServerConfig(host, port, corsPolicy)

}

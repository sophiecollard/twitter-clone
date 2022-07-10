package twitterclone.config

import org.http4s.server.middleware.CORSConfig

import scala.annotation.nowarn

@nowarn("cat=deprecation")
final case class ServerConfig(
  host: String,
  port: Int,
  cors: CORSConfig
)

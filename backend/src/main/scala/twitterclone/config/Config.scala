package twitterclone.config

import ciris.{ConfigValue, Effect, env}

final case class Config(
  environment: Environment,
  server: ServerConfig
)

object Config {

  val configValue: ConfigValue[Effect, Config] =
    for {
      environment <- env("ENVIRONMENT").as[Environment]
      server <- ServerConfig.configValue
    } yield Config(environment, server)

}

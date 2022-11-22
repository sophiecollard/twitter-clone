package twitterclone.config

import ciris.{ConfigValue, Effect, env}

sealed trait Config {
  def server: ServerConfig
}

object Config {

  final case class Local(
    server: ServerConfig
  ) extends Config

  final case class Production(
    postgres: PostgresConfig,
    server: ServerConfig
  ) extends Config

  val localConfigValue: ConfigValue[Effect, Config] =
    ServerConfig.configValue.map { server =>
      Local(server)
    }

  val productionConfigValue: ConfigValue[Effect, Config] =
    for {
      postgres <- PostgresConfig.configValue
      server <- ServerConfig.configValue
    } yield Production(postgres, server)

  val configValue: ConfigValue[Effect, Config] =
    env("ENVIRONMENT").as[Environment].default(Environment.Local).flatMap[Effect, Config] {
      case Environment.Local      => localConfigValue
      case Environment.Production => productionConfigValue
    }

}

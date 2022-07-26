package twitterclone.config

import ciris._

final case class PostgresConfig(
  database: String,
  user: String,
  password: Secret[String]
)

object PostgresConfig {

  val configValue: ConfigValue[Effect, PostgresConfig] =
    for {
      database <- env("POSTGRES_DB").as[String]
      user <- env("POSTGRES_USER").as[String]
      password <- env("POSTGRES_PASSWORD").as[String].secret
    } yield PostgresConfig(database, user, password)

}

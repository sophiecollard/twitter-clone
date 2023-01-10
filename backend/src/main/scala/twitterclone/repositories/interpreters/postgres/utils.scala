package twitterclone.repositories.interpreters.postgres

import cats.effect.kernel.Async
import doobie.util.transactor.Transactor
import twitterclone.config.PostgresConfig
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult

object utils {

  def getTransactor[F[_]: Async](postgresConfig: PostgresConfig): Transactor[F] =
    Transactor.fromDriverManager[F](
      driver = "org.postgresql.Driver",
      url = s"jdbc:postgresql:${postgresConfig.database}",
      user = postgresConfig.user,
      pass = postgresConfig.password.value
    )

  def runMigrations[F[_]: Async](postgresConfig: PostgresConfig): F[MigrateResult] =
    Async[F].delay {
      Flyway
        .configure()
        .dataSource(
          s"jdbc:postgresql:${postgresConfig.database}",
          postgresConfig.user,
          postgresConfig.password.value
        )
        .load()
        .migrate()
    }

}

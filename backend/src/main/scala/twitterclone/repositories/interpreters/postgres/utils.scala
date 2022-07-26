package twitterclone.repositories.interpreters.postgres

import cats.effect.kernel.Async
import doobie.util.transactor.Transactor
import twitterclone.config.PostgresConfig

object utils {

  def getTransactor[F[_]: Async](postgresConfig: PostgresConfig): Transactor[F] =
    Transactor.fromDriverManager[F](
      driver = "org.postgresql.Driver",
      url = s"jdbc:postgresql:${postgresConfig.database}",
      user = postgresConfig.user,
      pass = postgresConfig.password.value
    )

}

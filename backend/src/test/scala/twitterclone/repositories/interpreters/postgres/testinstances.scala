package twitterclone.repositories.interpreters.postgres

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import doobie.implicits._
import doobie.{ConnectionIO, Transactor}

object testinstances {

  implicit class ConnectionIOOps[A](value: ConnectionIO[A]) {
    def unsafe(implicit xa: Transactor[IO], r: IORuntime): A =
      value.transact(xa).unsafeRunSync()
  }

}

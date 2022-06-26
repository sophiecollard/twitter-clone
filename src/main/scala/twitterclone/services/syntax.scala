package twitterclone.services

import cats.~>

object syntax {

  implicit class Transactable[F[_], G[_], A](private val value: G[A]) {
    def transact(implicit transactor: G ~> F): F[A] =
      transactor(value)
  }

}

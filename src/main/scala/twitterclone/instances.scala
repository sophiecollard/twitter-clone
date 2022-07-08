package twitterclone

import cats.arrow.FunctionK
import cats.effect.IO
import cats.~>

object instances {

  implicit val ioTransactor: FunctionK[IO, IO] =
    new ~>[IO, IO] {
      override def apply[A](fa: IO[A]): IO[A] =
        fa
    }

}

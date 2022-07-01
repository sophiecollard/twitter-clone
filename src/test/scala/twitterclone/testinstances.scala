package twitterclone

import cats.arrow.FunctionK
import cats.{Id => CatsId}
import cats.~>

object testinstances {

  implicit val catsIdTransactor: FunctionK[CatsId, CatsId] =
    new ~>[CatsId, CatsId] {
      override def apply[A](fa: CatsId[A]): CatsId[A] =
        fa
    }

}

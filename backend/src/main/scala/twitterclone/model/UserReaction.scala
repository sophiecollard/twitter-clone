package twitterclone.model

import io.circe.{Decoder, Encoder, Json}

sealed trait UserReaction[+A] extends Product with Serializable

object UserReaction {

  final case class AuthedUserReaction[A](value: A) extends UserReaction[A]

  final case object UserNotAuthenticated extends UserReaction[Nothing]

  implicit def decoder[A](implicit ev: Decoder[A]): Decoder[UserReaction[A]] =
    ev.map(AuthedUserReaction.apply).or(
      Decoder.decodeString.emap {
        case "UserNotAuthenticated" => Right(UserNotAuthenticated)
        case other                  => Left(s"Not a user reaction: $other")
      }
    )

  implicit def encoder[A](implicit ev: Encoder[A]): Encoder[UserReaction[A]] =
    Encoder.instance[UserReaction[A]] {
      case AuthedUserReaction(a) => ev(a)
      case UserNotAuthenticated  => Json.fromString("UserNotAuthenticated")
    }

}

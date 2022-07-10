package twitterclone.config

sealed trait Environment

object Environment {
  case object Local extends Environment
  case object Live extends Environment
}

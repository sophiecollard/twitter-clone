package twitterclone.config

import ciris.ConfigDecoder

sealed trait Environment

object Environment {

  case object Local      extends Environment
  case object Production extends Environment

  implicit val configDecoder: ConfigDecoder[String, Environment] =
    ConfigDecoder[String, String].mapOption("Environment") {
      case "local" => Some(Local)
      case "prod"  => Some(Production)
      case _       => None
    }

}

package twitterclone.config

import ciris.ConfigDecoder

object instances {

  implicit val commaSeparatedStringSet: ConfigDecoder[String, Set[String]] =
    ConfigDecoder[String].map { string =>
      val pattern = "(?<=\\[)([^,]+)?(,[^,]+)*(?=])".r
      pattern
        .findAllIn(string).toSet
        .filterNot(_.isEmpty)
        .flatMap(_.split(","))
    }

}

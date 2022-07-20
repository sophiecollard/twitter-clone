package twitterclone.model.user

sealed abstract case class Name(value: String)

object Name {

  private val pattern = "^[a-zA-Z0-9-_ ]{1,36}$".r

  def fromString(value: String): Either[String, Name] =
    pattern findFirstIn value match {
      case Some(nameValue) =>
        Right(new Name(nameValue) {})
      case None =>
        Left(s"$value does not match the following regex: ${pattern.toString}")
    }

  def unsafeFromString(value: String): Name =
    fromString(value) match {
      case Right(name) => name
      case Left(error) => throw new RuntimeException(error)
    }

}

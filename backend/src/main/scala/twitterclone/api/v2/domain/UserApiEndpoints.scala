package twitterclone.api.v2.domain

import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe.jsonBody
import twitterclone.api.error.ApiError
import twitterclone.api.model.UserSignUpRequest
import twitterclone.model.user.User

object UserApiEndpoints {

  lazy val createUserEndpoint: PublicEndpoint[UserSignUpRequest, ApiError, User, Any] =
    endpoint.post
      .in("users")
      .in(jsonBody[UserSignUpRequest])
      .out(jsonBody[User])
      .errorOut(jsonBody[ApiError])
      .description("Create a new user with the specified handle and username")

  lazy val allEndpoints: List[AnyEndpoint] =
    List(createUserEndpoint).map(_.tag("Users"))

}

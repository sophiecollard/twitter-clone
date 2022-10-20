package twitterclone.api.user

import io.circe.Json
import io.circe.syntax._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import twitterclone.model.user.{Bio, Handle, Name}

class CreateUserRequestSpec extends AnyWordSpec with Matchers {
  "The Decoder instance" should {
    "decode an instance of CreateUserRequest from JSON" in new CreateUserRequestFixtures {
      json.as[CreateUserRequest] shouldBe Right(request)
    }
  }

  "The Encoder instance" should {
    "encode an instance of CreateUserRequest to JSON" in new CreateUserRequestFixtures {
      request.asJson shouldBe json
    }
  }
}

trait CreateUserRequestFixtures {

  val json: Json =
    Json.obj(fields =
      "handle" := "odersky",
      "name" := "Martin Odersky",
      "bio" := "I created Scala"
    )

  val request: CreateUserRequest =
    CreateUserRequest(
      handle = Handle.unsafeFromString("odersky"),
      name = Name.unsafeFromString("Martin Odersky"),
      bio = Bio("I created Scala")
    )

}

package twitterclone.config

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class InstancesSpec extends AnyWordSpec with Matchers {
  "commaSeparatedStringSet" should {
    "decode an empty set" in {
      instances.commaSeparatedStringSet.decode(None, "[]") shouldBe Right(Set.empty[String])
    }

    "decode a set with one element" in {
      instances.commaSeparatedStringSet.decode(None, "[http://localhost:8000]") shouldBe
        Right(Set("http://localhost:8000"))
    }

    "decode a set with multiple elements" in {
      instances.commaSeparatedStringSet.decode(None, "[ga,bu,zo,meu]") shouldBe
        Right(Set("ga", "bu", "zo", "meu"))

      instances.commaSeparatedStringSet.decode(None, "[http://localhost:8000,https://localhost:6060]") shouldBe
        Right(Set("http://localhost:8000", "https://localhost:6060"))
    }
  }
}

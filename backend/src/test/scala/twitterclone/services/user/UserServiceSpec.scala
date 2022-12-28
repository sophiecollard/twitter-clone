package twitterclone.services.user

import cats.{Id => CatsId}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import twitterclone.fixtures.user._
import twitterclone.model.Id
import twitterclone.model.user.{Handle, Name, Status, User}
import twitterclone.repositories.interpreters.local.LocalUserRepository
import twitterclone.services.error.ServiceError.{ResourceNotFound, UserHandleAlreadyExists, UserHandleNotFound}
import twitterclone.testinstances._
import twitterclone.testsyntax._

import scala.collection.concurrent.TrieMap

class UserServiceSpec extends AnyWordSpec with Matchers {
  "The create method" when {
    "the specified handle does not exist" should {
      "create and return a new user with status 'PendingActivation'" in new Fixtures {
        private val repoState = TrieMap.empty[Id[User], User]
        private val service = newService(repoState)
        private val handle = Handle.unsafeFromString("anna")
        private val name = Name.unsafeFromString("Anna11")

        withNoServiceError(service.create(handle, name)) { returnedUser =>
          returnedUser.handle.value shouldBe "anna"
          returnedUser.name.value shouldBe "Anna11"
          returnedUser.status shouldBe Status.PendingActivation
        }
      }
    }

    "the specified handle already exists" should {
      "return an error" in new Fixtures {
        private val repoState = TrieMap.from((activeUser.id, activeUser) :: Nil)
        private val service = newService(repoState)
        private val name = Name.unsafeFromString("Anna11")

        withServiceError(service.create(activeUser.handle, name)) { error =>
          error shouldBe UserHandleAlreadyExists(activeUser.handle)
        }
      }
    }
  }

  "The get method" when {
    "the specified user id exists" should {
      "get the user" in new Fixtures {
        private val repoState = TrieMap.from((activeUser.id, activeUser) :: Nil)
        private val service = newService(repoState)

        withNoServiceError(service.get(activeUser.id)) { returnedUser =>
          returnedUser shouldBe activeUser
        }
      }
    }

    "the specified user id doesn't exist" should {
      "return an error" in new Fixtures {
        private val repoState = TrieMap.from((activeUser.id, activeUser) :: Nil)
        private val service = newService(repoState)
        private val randomUserId = Id.random[User]

        withServiceError(service.get(randomUserId)) { error =>
          error shouldBe ResourceNotFound(randomUserId, "User")
        }
      }
    }
  }

  "The getByHandle method" when {
    "the specified handle exists" should {
      "get the user" in new Fixtures {
        private val repoState = TrieMap.from((activeUser.id, activeUser) :: Nil)
        private val service = newService(repoState)

        withNoServiceError(service.getByHandle(activeUser.handle)) { returnedUser =>
          returnedUser shouldBe activeUser
        }
      }
    }

    "the specified handle doesn't exist" should {
      "return an error" in new Fixtures {
        private val repoState = TrieMap.from((activeUser.id, activeUser) :: Nil)
        private val service = newService(repoState)
        private val unknownUserHandle = Handle.unsafeFromString("martin")

        withServiceError(service.getByHandle(unknownUserHandle)) { error =>
          error shouldBe UserHandleNotFound(unknownUserHandle)
        }
      }
    }
  }
}

trait Fixtures {

  def newService(repoState: TrieMap[Id[User], User]): UserService[CatsId] = {
    val userRepository = LocalUserRepository.create[CatsId](repoState)
    UserService.create[CatsId, CatsId](userRepository)
  }

}

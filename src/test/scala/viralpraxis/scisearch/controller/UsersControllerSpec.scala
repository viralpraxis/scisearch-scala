package viralpraxis.scisearch.controller

import org.scalatest.EitherValues
import org.scalatest.matchers.should.Matchers
import sttp.client3.testing.SttpBackendStub
import sttp.client3.{UriContext, basicRequest}
import sttp.tapir.server.stub.TapirStubInterpreter
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import sttp.tapir.integ.cats.effect.CatsMonadError

import java.util.UUID

import org.scalatest.funspec.AnyFunSpec
import tethys._
import tethys.jackson._
import viralpraxis.scisearch.common.cache.Cache
import viralpraxis.scisearch.domain.user.User

import viralpraxis.scisearch.domain.user.UserResponse
import viralpraxis.scisearch.service.RepositoryUserService
import viralpraxis.scisearch.repository.resident.UserRepositoryInMemory

class UserControllerSpec extends AnyFunSpec with Matchers with EitherValues {
  describe("POST /api/v1/users") {
    it("persists user") {
      val usersCache = Cache.make[IO, UUID, User].unsafeRunSync()
      val usersRepo = UserRepositoryInMemory(usersCache)

      val backendStub = TapirStubInterpreter(SttpBackendStub(new CatsMonadError[IO]()))
        .whenServerEndpointRunLogic(
          UsersController
            .make(RepositoryUserService(usersRepo))
            .createUser,
        )
        .backend()

      val response = basicRequest
        .post(uri"http://test.com/api/v1/users")
        .body(
          s"{\"email\": \"${UsersControllerSpec.userEmail}\",\"password\":\"${UsersControllerSpec.userEmail}\"}",
        )
        .send(backendStub)

      val user = response
        .unsafeRunSync()
        .body
        .value
        .jsonAs[UserResponse]

      user.map(_.email shouldBe UsersControllerSpec.userEmail)
      response.map(_.code.code shouldBe 200).unsafeRunSync()
    }
  }
}

object UsersControllerSpec {
  lazy val userEmail = "test@test.com"
  lazy val userPassword = "foobar"
}

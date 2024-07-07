package viralpraxis.scisearch.controller

import java.util.UUID
import java.util.Base64
import java.time.Instant
import java.nio.charset.StandardCharsets

import org.scalatest.EitherValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.funspec.AnyFunSpec

import tethys._
import tethys.jackson._

import sttp.client3.testing.SttpBackendStub
import sttp.client3.{UriContext, basicRequest}
import sttp.tapir.server.stub.TapirStubInterpreter
import sttp.tapir.integ.cats.effect.CatsMonadError

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.effect.std.Random

import viralpraxis.scisearch.domain.user.User
import viralpraxis.scisearch.common.cache.Cache
import viralpraxis.scisearch.domain.bookmark.Bookmark
import viralpraxis.scisearch.service.RepositoryBookmarkService
import viralpraxis.scisearch.repository.resident.BookmarkRepositoryInMemory
import viralpraxis.scisearch.repository.resident.UserRepositoryInMemory
import viralpraxis.scisearch.domain.bookmark.BookmarkResponse
import viralpraxis.scisearch.service.RepositoryUserService
import viralpraxis.scisearch.repository.UserRepository

class BookmarkControllerSpec extends AnyFunSpec with Matchers with EitherValues {
  implicit val random: Random[IO] = Random.scalaUtilRandom[IO].unsafeRunSync()

  describe("POST /api/v1/bookmarks") {
    it("persists bookmark") {
      val bookmarksCache = Cache.make[IO, UUID, Bookmark].unsafeRunSync()
      val usersCache = Cache.make[IO, UUID, User].unsafeRunSync()

      val bookmarksRepo = BookmarkRepositoryInMemory(bookmarksCache)
      val userRepo = UserRepositoryInMemory(usersCache)

      val backendStub = TapirStubInterpreter(SttpBackendStub(new CatsMonadError[IO]()))
        .whenServerEndpointRunLogic(
          BookmarksController
            .make(RepositoryBookmarkService(bookmarksRepo), RepositoryUserService(userRepo))
            .createBookmarks,
        )
        .backend()

      BookmarksControllerSpec.createUser(userRepo)

      val unauthorizedResponse = basicRequest
        .post(uri"http://test.com/api/v1/bookmarks")
        .header("Authorization", BookmarksControllerSpec.invalidAuthHeader)
        .body("{\"identifier\": \"foobar\"}")
        .send(backendStub)

      val response = basicRequest
        .post(uri"http://test.com/api/v1/bookmarks")
        .header("Authorization", BookmarksControllerSpec.authHeader)
        .body("{\"identifier\": \"foobar\"}")
        .send(backendStub)

      val bookmark = response
        .unsafeRunSync()
        .body
        .value
        .jsonAs[BookmarkResponse]

      bookmark.map(_.identifier shouldBe "foobar")
      response.map(_.code.code shouldBe 200).unsafeRunSync()
      unauthorizedResponse.map(_.code.code shouldBe 400).unsafeRunSync()
    }
  }

  describe("GET /api/v1/bookmarks/:id") {
    it("renders bookmark if record is found") {
      val bookmarksCache = Cache.make[IO, UUID, Bookmark].unsafeRunSync()
      val usersCache = Cache.make[IO, UUID, User].unsafeRunSync()

      val bookmarksRepo = BookmarkRepositoryInMemory(bookmarksCache)
      val userRepo = UserRepositoryInMemory(usersCache)

      val backendStub = TapirStubInterpreter(SttpBackendStub(new CatsMonadError[IO]()))
        .whenServerEndpointRunLogic(
          BookmarksController
            .make(RepositoryBookmarkService(bookmarksRepo), RepositoryUserService(userRepo))
            .getBookmark,
        )
        .backend()

      val id: UUID = UUID.fromString("69f5fc4b-2f6b-468a-8417-e2c5c9b9f2ac")

      val user = BookmarksControllerSpec.createUser(userRepo)

      bookmarksCache
        .add(id, Bookmark(id, "foobar", None, Instant.now(), Instant.now(), user.id))
        .unsafeRunSync()

      val response = basicRequest
        .get(uri"http://test.com/api/v1/bookmarks/${id.toString()}")
        .header("Authorization", BookmarksControllerSpec.authHeader)
        .send(backendStub)

      val bookmark = response
        .unsafeRunSync()
        .body
        .value
        .jsonAs[BookmarkResponse]

      bookmark.map(_.id shouldBe id)
      response.map(_.code.code shouldBe 200).unsafeRunSync()
    }

    it("renders 400 if record is not found") {
      val bookmarksCache = Cache.make[IO, UUID, Bookmark].unsafeRunSync()
      val usersCache = Cache.make[IO, UUID, User].unsafeRunSync()

      val bookmarksRepo = BookmarkRepositoryInMemory(bookmarksCache)
      val userRepo = UserRepositoryInMemory(usersCache)

      val backendStub = TapirStubInterpreter(SttpBackendStub(new CatsMonadError[IO]()))
        .whenServerEndpointRunLogic(
          BookmarksController
            .make(RepositoryBookmarkService(bookmarksRepo), RepositoryUserService(userRepo))
            .getBookmark,
        )
        .backend()

      val id: UUID = UUID.fromString("69f5fc4b-2f6b-468a-8417-e2c5c9b9f2ac")

      BookmarksControllerSpec.createUser(userRepo)

      val response = basicRequest
        .get(uri"http://test.com/api/v1/bookmarks/${id.toString()}")
        .header("Authorization", BookmarksControllerSpec.authHeader)
        .send(backendStub)

      response.map(_.body.value shouldBe "")
      response.map(_.code.code shouldBe 400).unsafeRunSync()
    }
  }

  describe("GET /api/v1/bookmarks") {
    it("renders list of bookmarks") {
      val bookmarksCache = Cache.make[IO, UUID, Bookmark].unsafeRunSync()
      val usersCache = Cache.make[IO, UUID, User].unsafeRunSync()

      val bookmarksRepo = BookmarkRepositoryInMemory(bookmarksCache)
      val userRepo = UserRepositoryInMemory(usersCache)

      val backendStub = TapirStubInterpreter(SttpBackendStub(new CatsMonadError[IO]()))
        .whenServerEndpointRunLogic(
          BookmarksController
            .make(RepositoryBookmarkService(bookmarksRepo), RepositoryUserService(userRepo))
            .listBookmarks,
        )
        .backend()

      val user = BookmarksControllerSpec.createUser(userRepo)

      val id1: UUID = UUID.fromString("69f5fc4b-2f6b-468a-8417-e2c5c9b9f2ac")
      val id2: UUID = UUID.fromString("a439b468-7563-4a63-9231-89584c7a0e88")

      List(
        Bookmark(id1, "bookmark-1", None, Instant.now(), Instant.now(), user.id),
        Bookmark(id2, "bookmark-2", None, Instant.now(), Instant.now(), user.id),
      ).foreach(bookmark => bookmarksCache.add(bookmark.id, bookmark).unsafeRunSync())

      val response = basicRequest
        .get(uri"http://test.com/api/v1/bookmarks")
        .header("Authorization", BookmarksControllerSpec.authHeader)
        .send(backendStub)

      val bookmark = response
        .unsafeRunSync()
        .body
        .value
        .jsonAs[List[BookmarkResponse]]

      bookmark.map(_.map(_.id).sorted shouldBe Seq(id1, id2).sorted)
      response.map(_.code.code shouldBe 200).unsafeRunSync()
    }
  }

  describe("DELETE /api/v1/bookmarks/:id") {
    it("deletes bookmark by ID") {
      val bookmarksCache = Cache.make[IO, UUID, Bookmark].unsafeRunSync()
      val usersCache = Cache.make[IO, UUID, User].unsafeRunSync()

      val bookmarksRepo = BookmarkRepositoryInMemory(bookmarksCache)
      val userRepo = UserRepositoryInMemory(usersCache)

      val backendStub = TapirStubInterpreter(SttpBackendStub(new CatsMonadError[IO]()))
        .whenServerEndpointRunLogic(
          BookmarksController
            .make(RepositoryBookmarkService(bookmarksRepo), RepositoryUserService(userRepo))
            .deleteBookmark,
        )
        .backend()

      val id: UUID = UUID.fromString("69f5fc4b-2f6b-468a-8417-e2c5c9b9f2ac")

      val user = BookmarksControllerSpec.createUser(userRepo)

      bookmarksCache
        .add(id, Bookmark(id, "foobar", None, Instant.now(), Instant.now(), user.id))
        .unsafeRunSync()

      val response = basicRequest
        .delete(uri"http://test.com/api/v1/bookmarks/${id.toString()}")
        .header("Authorization", BookmarksControllerSpec.authHeader)
        .send(backendStub)

      val notFoundResponse = basicRequest
        .delete(uri"http://test.com/api/v1/bookmarks/${UUID.randomUUID}")
        .header("Authorization", BookmarksControllerSpec.authHeader)
        .send(backendStub)

      response.map(_.code.code shouldBe 204).unsafeRunSync()
      notFoundResponse.map(_.code.code shouldBe 400).unsafeRunSync()
      notFoundResponse.map(_.body.left.value shouldBe "{\"message\":\"Not Found\"}").unsafeRunSync()
    }
  }

  describe("PATCH /api/v1/bookmarks/:id") {
    it("updates bookmark by ID") {
      val bookmarksCache = Cache.make[IO, UUID, Bookmark].unsafeRunSync()
      val usersCache = Cache.make[IO, UUID, User].unsafeRunSync()

      val bookmarksRepo = BookmarkRepositoryInMemory(bookmarksCache)
      val userRepo = UserRepositoryInMemory(usersCache)

      val backendStub = TapirStubInterpreter(SttpBackendStub(new CatsMonadError[IO]()))
        .whenServerEndpointRunLogic(
          BookmarksController
            .make(RepositoryBookmarkService(bookmarksRepo), RepositoryUserService(userRepo))
            .updateBookmarks,
        )
        .backend()

      val id: UUID = UUID.fromString("69f5fc4b-2f6b-468a-8417-e2c5c9b9f2ac")

      val user = BookmarksControllerSpec.createUser(userRepo)

      bookmarksCache
        .add(id, Bookmark(id, "foobar", None, Instant.now(), Instant.now(), user.id))
        .unsafeRunSync()

      val response = basicRequest
        .patch(uri"http://test.com/api/v1/bookmarks/${id.toString()}")
        .header("Authorization", BookmarksControllerSpec.authHeader)
        .body("{\"comment\": \"foobar-updated\"}")
        .send(backendStub)

      val notFoundResponse = basicRequest
        .patch(uri"http://test.com/api/v1/bookmarks/${UUID.randomUUID}")
        .header("Authorization", BookmarksControllerSpec.authHeader)
        .body("{\"comment\": \"foobar-updated\"}")
        .send(backendStub)

      response.map(_.code.code shouldBe 204).unsafeRunSync()
      notFoundResponse.map(_.code.code shouldBe 400).unsafeRunSync()
      notFoundResponse.map(_.body.left.value shouldBe "{\"message\":\"Not Found\"}").unsafeRunSync()
    }
  }

}

object BookmarksControllerSpec {
  lazy val userEmail = "test@test.com"
  lazy val userPassword = "foobar"

  def createUser(repository: UserRepository[IO]): User = {
    val user = User(
      UUID.randomUUID(),
      userEmail,
      userPassword,
      Instant.now(),
      Instant.now(),
    )

    repository.create(user).unsafeRunSync()
    user
  }

  def authHeader: String = {
    val credential =
      Base64.getEncoder.encodeToString(s"$userEmail:$userPassword".getBytes(StandardCharsets.UTF_8))

    s"Basic $credential"
  }

  def invalidAuthHeader: String = {
    val credential =
      Base64.getEncoder.encodeToString(s"username:password".getBytes(StandardCharsets.UTF_8))

    s"Basic $credential"
  }
}

package viralpraxis.scisearch.controller

import org.scalatest.EitherValues
import org.scalatest.matchers.should.Matchers
import sttp.client3.testing.SttpBackendStub
import sttp.client3.{UriContext, basicRequest}
import sttp.tapir.server.stub.TapirStubInterpreter
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import sttp.tapir.integ.cats.effect.CatsMonadError

import java.util.Base64
import java.nio.charset.StandardCharsets

import java.util.UUID

import org.scalatest.funspec.AnyFunSpec

import viralpraxis.scisearch.domain.user.User
import viralpraxis.scisearch.common.cache.Cache
import viralpraxis.scisearch.domain.subscription.Subscription
import viralpraxis.scisearch.service.RepositorySubscriptionService
import viralpraxis.scisearch.repository.resident.SubscriptionRepositoryInMemory
import viralpraxis.scisearch.repository.resident.UserRepositoryInMemory

import tethys._
import tethys.jackson._
import viralpraxis.scisearch.domain.subscription.SubscriptionResponse
import java.time.Instant
import viralpraxis.scisearch.service.RepositoryUserService
import viralpraxis.scisearch.repository.UserRepository
import cats.effect.std.Random

class SubscriptionControllerSpec extends AnyFunSpec with Matchers with EitherValues {
  implicit val random: Random[IO] = Random.scalaUtilRandom[IO].unsafeRunSync()

  describe("POST /api/v1/subscriptions") {
    it("persists subscription") {
      val subscriptionCache = Cache.make[IO, UUID, Subscription].unsafeRunSync()
      val usersCache = Cache.make[IO, UUID, User].unsafeRunSync()

      val subscriptionRepo = SubscriptionRepositoryInMemory(subscriptionCache)
      val userRepo = UserRepositoryInMemory(usersCache)

      val backendStub = TapirStubInterpreter(SttpBackendStub(new CatsMonadError[IO]()))
        .whenServerEndpointRunLogic(
          SubscriptionsController
            .make(RepositorySubscriptionService(subscriptionRepo), RepositoryUserService(userRepo))
            .createSubscriptions,
        )
        .backend()

      val user = SubscriptionsControllerSpec.createUser(userRepo)

      val unauthorizedResponse = basicRequest
        .post(uri"http://test.com/api/v1/subscriptions")
        .header("Authorization", SubscriptionsControllerSpec.authHeader + "_")
        .body(
          s"{\"keyword\": \"category-theory\",\"notificationPeriodInDays\":7,\"userId\":\"${user.id}\"}",
        )
        .send(backendStub)

      val response = basicRequest
        .post(uri"http://test.com/api/v1/subscriptions")
        .header("Authorization", SubscriptionsControllerSpec.authHeader)
        .body(
          s"{\"keyword\": \"category-theory\",\"notificationPeriodInDays\":7,\"userId\":\"${user.id}\"}",
        )
        .send(backendStub)

      val subscription = response
        .unsafeRunSync()
        .body
        .value
        .jsonAs[SubscriptionResponse]

      subscription.map(_.keyword shouldBe "category-theory")
      response.map(_.code.code shouldBe 200).unsafeRunSync()
      unauthorizedResponse.map(_.code.code shouldBe 400).unsafeRunSync()
    }
  }

  describe("GET /api/v1/subscriptions") {
    it("lists subscriptions for user") {
      val subscriptionCache = Cache.make[IO, UUID, Subscription].unsafeRunSync()
      val usersCache = Cache.make[IO, UUID, User].unsafeRunSync()

      val subscriptionRepo = SubscriptionRepositoryInMemory(subscriptionCache)
      val userRepo = UserRepositoryInMemory(usersCache)

      val backendStub = TapirStubInterpreter(SttpBackendStub(new CatsMonadError[IO]()))
        .whenServerEndpointRunLogic(
          SubscriptionsController
            .make(RepositorySubscriptionService(subscriptionRepo), RepositoryUserService(userRepo))
            .listSubscriptions,
        )
        .backend()

      val user = SubscriptionsControllerSpec.createUser(userRepo)

      val id1: UUID = UUID.fromString("69f5fc4b-2f6b-468a-8417-e2c5c9b9f2ac")
      val id2: UUID = UUID.fromString("a439b468-7563-4a63-9231-89584c7a0e88")

      List(
        Subscription(id1, "catas-1", 7, Instant.now(), Instant.now(), None, user.id),
        Subscription(id2, "catas-2", 14, Instant.now(), Instant.now(), None, user.id),
      ).foreach(subscription =>
        subscriptionCache.add(subscription.id, subscription).unsafeRunSync(),
      )

      val unauthorizedResponse = basicRequest
        .get(uri"http://test.com/api/v1/subscriptions")
        .header("Authorization", SubscriptionsControllerSpec.authHeader + "_")
        .send(backendStub)

      val response = basicRequest
        .get(uri"http://test.com/api/v1/subscriptions")
        .header("Authorization", SubscriptionsControllerSpec.authHeader)
        .send(backendStub)

      val subscriptions = response
        .unsafeRunSync()
        .body
        .value
        .jsonAs[List[SubscriptionResponse]]

      subscriptions.map(_.map(_.id).sorted shouldBe Seq(id1, id2).sorted)
      response.map(_.code.code shouldBe 200).unsafeRunSync()
      unauthorizedResponse.map(_.code.code shouldBe 400).unsafeRunSync()
    }
  }

  describe("GET /api/v1/subscriptions/:id") {
    it("finds subscription") {
      val subscriptionCache = Cache.make[IO, UUID, Subscription].unsafeRunSync()
      val usersCache = Cache.make[IO, UUID, User].unsafeRunSync()

      val subscriptionRepo = SubscriptionRepositoryInMemory(subscriptionCache)
      val userRepo = UserRepositoryInMemory(usersCache)

      val backendStub = TapirStubInterpreter(SttpBackendStub(new CatsMonadError[IO]()))
        .whenServerEndpointRunLogic(
          SubscriptionsController
            .make(RepositorySubscriptionService(subscriptionRepo), RepositoryUserService(userRepo))
            .getSubscription,
        )
        .backend()

      val user = SubscriptionsControllerSpec.createUser(userRepo)

      val id: UUID = UUID.fromString("69f5fc4b-2f6b-468a-8417-e2c5c9b9f2ac")

      val subscription: Subscription =
        Subscription(id, "catas", 7, Instant.now(), Instant.now(), None, user.id)
      subscriptionCache.add(subscription.id, subscription).unsafeRunSync()

      val unauthorizedResponse = basicRequest
        .get(uri"http://test.com/api/v1/subscriptions/${subscription.id}")
        .header("Authorization", SubscriptionsControllerSpec.authHeader + "_")
        .send(backendStub)

      val notFoundResponse = basicRequest
        .get(uri"http://test.com/api/v1/subscriptions/${subscription.id}123")
        .header("Authorization", SubscriptionsControllerSpec.authHeader)
        .send(backendStub)

      val response = basicRequest
        .get(uri"http://test.com/api/v1/subscriptions/${subscription.id}")
        .header("Authorization", SubscriptionsControllerSpec.authHeader)
        .send(backendStub)

      val subscriptionResponse = response
        .unsafeRunSync()
        .body
        .value
        .jsonAs[SubscriptionResponse]

      subscriptionResponse.map(_.id shouldBe id)
      response.map(_.code.code shouldBe 200).unsafeRunSync()
      unauthorizedResponse.map(_.code.code shouldBe 400).unsafeRunSync()
      notFoundResponse.map(_.code.code shouldBe 400).unsafeRunSync()
    }
  }
}

object SubscriptionsControllerSpec {
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
}

package viralpraxis.scisearch.repository.database

import cats.effect.testing.scalatest.{AsyncIOSpec, CatsResourceIO}
import cats.effect.{IO, Resource}
import com.dimafeng.testcontainers.PostgreSQLContainer
import doobie.Transactor
import org.scalatest.flatspec.FixtureAsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import org.testcontainers.containers.wait.strategy.{
  LogMessageWaitStrategy,
  Wait,
  WaitAllStrategy,
  WaitStrategy,
}
import org.testcontainers.utility.DockerImageName
import viralpraxis.scisearch.config.{AppConfig, PostgreSQLConfig, TokenConfig}
import viralpraxis.scisearch.database.FlywayMigration
import viralpraxis.scisearch.repository.database.UserDatabaseRepo
import viralpraxis.scisearch.database.transactor.makeTransactor

import java.util.UUID
import java.time.Instant
import viralpraxis.scisearch.domain.user.User

class UsersDatabaseRepoSpec
    extends FixtureAsyncFlatSpec
    with AsyncIOSpec
    with CatsResourceIO[Transactor[IO]]
    with Matchers {

  override val resource: Resource[IO, Transactor[IO]] =
    for {
      c <- containerResource
      conf = PostgreSQLConfig(c.host, c.mappedPort(5432), c.databaseName, c.username, c.password, 2)
      _ <- Resource.eval(FlywayMigration.migrate[IO](conf))
      tx <- makeTransactor[IO](conf)
    } yield tx

  it should "Find user by credentials" in { implicit t =>
    val usersRepo = new UserDatabaseRepo[IO](UsersDatabaseRepoSpec.config)

    val now = Instant.now()
    val user = User(UUID.randomUUID(), "example@gmail.com", "foobar", now, now)

    for {
      _ <- usersRepo.create(user).asserting(_ shouldBe 1)
      _ <- usersRepo
        .findByCredential("example@gmail.com", Some("foobar"))
        .asserting(_ should not be empty)
    } yield ()
  }

  it should "Create user successfully" in { implicit t =>
    val usersRepo = new UserDatabaseRepo[IO](UsersDatabaseRepoSpec.config)

    val now = Instant.now()
    val user = User(UUID.randomUUID(), "test@gmail.com", "foobar", now, now)

    for {
      _ <- usersRepo.create(user).asserting(_ shouldBe 1)
    } yield ()
  }

  private val defaultWaitStrategy: WaitStrategy = new WaitAllStrategy()
    .withStrategy(Wait.forListeningPort())
    .withStrategy(
      new LogMessageWaitStrategy()
        .withRegEx(".*database system is ready to accept connections.*\\s")
        .withTimes(2),
    )

  private def containerResource: Resource[IO, PostgreSQLContainer] =
    Resource.make(
      IO {
        val c = PostgreSQLContainer
          .Def(
            dockerImageName = DockerImageName.parse("postgres:16.1"),
          )
          .start()
        c.container.waitingFor(defaultWaitStrategy)
        c
      },
    )(c => IO(c.stop()))
}

object UsersDatabaseRepoSpec {
  val pgConfig = PostgreSQLConfig("localhost", 5432, "scisearch", "postgres", "postgres", 1)
  val tokenConfig = TokenConfig("salt", "secret")
  val config = AppConfig("127.0.0.1", 3001, pgConfig, tokenConfig)
}

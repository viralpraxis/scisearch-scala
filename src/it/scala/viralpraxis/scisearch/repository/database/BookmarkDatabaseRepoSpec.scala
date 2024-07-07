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
import viralpraxis.scisearch.domain.user.User
import viralpraxis.scisearch.repository.database.UserDatabaseRepo
import viralpraxis.scisearch.database.transactor.makeTransactor

import java.util.UUID
import java.time.Instant
import viralpraxis.scisearch.domain.bookmark.Bookmark
import viralpraxis.scisearch.domain.bookmark.UpdateBookmark

class BookmarksDatabaseRepoSpec
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

  "BookmarksDatabaseRepo" should "return expected result if db is empty" in { implicit t =>
    val repo = new BookmarkDatabaseRepo[IO]
    val uuid = UUID.randomUUID()

    val user = User(UUID.randomUUID(), "yk@gmail.com", "foobar", null, null)

    for {
      _ <- repo.list(user).asserting(_ shouldBe List.empty)
      _ <- repo.get(user, uuid).asserting(_ shouldBe None)
      _ <- repo.delete(user, uuid).asserting(_ shouldBe 0)
    } yield ()
  }

  it should "Create bookmark successfully" in { implicit t =>
    val pgConfig = PostgreSQLConfig("localhost", 5432, "scisearch", "postgres", "postgres", 1)
    val tokenConfig = TokenConfig("salt", "secret")
    val config = AppConfig("127.0.0.1", 3001, pgConfig, tokenConfig)

    val bookmarksRepo = new BookmarkDatabaseRepo[IO]
    val usersRepo = new UserDatabaseRepo[IO](config)

    val now = Instant.now()

    val user = User(UUID.randomUUID(), "test@gmail.com", "foobar", now, now)
    val bookmark = Bookmark(UUID.randomUUID(), "identifier", None, now, now, user.id)

    for {
      _ <- usersRepo.create(user).asserting(_ shouldBe 1)
      _ <- bookmarksRepo.create(user, bookmark).asserting(_ shouldBe 1)
      _ <- bookmarksRepo.delete(user, bookmark.id).asserting(_ shouldBe 1)
    } yield ()
  }

  it should "Update bookmark successfully" in { implicit t =>
    val pgConfig = PostgreSQLConfig("localhost", 5432, "scisearch", "postgres", "postgres", 1)
    val tokenConfig = TokenConfig("salt", "secret")
    val config = AppConfig("127.0.0.1", 3001, pgConfig, tokenConfig)

    val bookmarksRepo = new BookmarkDatabaseRepo[IO]
    val usersRepo = new UserDatabaseRepo[IO](config)

    val now = Instant.now()

    val user = User(UUID.randomUUID(), "test-1@gmail.com", "foobar", now, now)
    val bookmark = Bookmark(UUID.randomUUID(), "identifier", Some("comment"), now, now, user.id)
    val updateBookmark = UpdateBookmark(Some("comment-updated"))

    for {
      _ <- usersRepo.create(user).asserting(_ shouldBe 1)
      _ <- bookmarksRepo.create(user, bookmark).asserting(_ shouldBe 1)
      _ <- bookmarksRepo.get(user, bookmark.id).asserting(_ should not be empty)
      _ <- bookmarksRepo.update(user, bookmark.id, updateBookmark).asserting(_ shouldBe 1)
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

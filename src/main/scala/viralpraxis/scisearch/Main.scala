package viralpraxis.scisearch

import doobie.Transactor

import cats.effect.{ExitCode, IO, IOApp}
import com.comcast.ip4s.{Host, Port}
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.client4.DefaultSyncBackend

import pureconfig.ConfigSource
import pureconfig.module.catseffect.syntax._

import viralpraxis.scisearch.controller.{BookmarksController, PapersController, UsersController}
import viralpraxis.scisearch.service.RepositoryPaperService
import viralpraxis.scisearch.service.RepositoryUserService
import viralpraxis.scisearch.repository.resident.PaperDatabaseRepo
import viralpraxis.scisearch.service.ArxivClient
import viralpraxis.scisearch.service.RepositoryBookmarkService
import viralpraxis.scisearch.repository.database.BookmarkDatabaseRepo
import viralpraxis.scisearch.repository.database.UserDatabaseRepo
import viralpraxis.scisearch.database.FlywayMigration
import viralpraxis.scisearch.config.AppConfig
import viralpraxis.scisearch.config.PureconfigReaders._
import viralpraxis.scisearch.database.transactor.makeTransactor
import viralpraxis.scisearch.controller.SubscriptionsController
import viralpraxis.scisearch.service.RepositorySubscriptionService
import viralpraxis.scisearch.repository.database.SubscriptionDatabaseRepo

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    ConfigSource.default.loadF[IO, AppConfig].flatMap { config =>
      makeTransactor[IO](config.database).use { implicit xa: Transactor[IO] =>
        for {
          arxivClient <- ArxivClient.make[IO](DefaultSyncBackend())

          _ <- FlywayMigration.migrate[IO](config.database)

          endpoints <- IO.delay {
            List(
              BookmarksController.make(
                RepositoryBookmarkService(new BookmarkDatabaseRepo[IO]),
                RepositoryUserService(new UserDatabaseRepo[IO](config)),
              ),
              PapersController.make(
                RepositoryPaperService(PaperDatabaseRepo(arxivClient)),
              ),
              UsersController.make(RepositoryUserService(new UserDatabaseRepo[IO](config))),
              SubscriptionsController.make(
                RepositorySubscriptionService(new SubscriptionDatabaseRepo[IO]()),
                RepositoryUserService(new UserDatabaseRepo[IO](config)),
              ),
            ).flatMap(_.endpoints)
          }
          swagger = SwaggerInterpreter()
            .fromServerEndpoints[IO](endpoints, "scisearch", "1.0.0")
          routes = Http4sServerInterpreter[IO]()
            .toRoutes(swagger ++ endpoints)
          _ <- EmberServerBuilder
            .default[IO]
            .withHost(Host.fromString(config.bind).get)
            .withPort(Port.fromInt(config.port).get)
            .withHttpApp(Router("/" -> routes).orNotFound)
            .build
            .useForever
        } yield ExitCode.Success
      }
    }
}

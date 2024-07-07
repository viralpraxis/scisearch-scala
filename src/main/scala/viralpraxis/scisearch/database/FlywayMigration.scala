package viralpraxis.scisearch.database

import cats.effect.Sync
import cats.syntax.functor._
import org.flywaydb.core.Flyway
import viralpraxis.scisearch.config.PostgreSQLConfig

/** Facade for `Flyway` migrator interface. */
object FlywayMigration {

  /** Apply pending migrations. */
  def migrate[F[_]](config: PostgreSQLConfig)(implicit F: Sync[F]): F[Unit] =
    F.delay(loadFlyway(config).migrate()).void

  // $COVERAGE-OFF$
  /** Cleanup migrations. */
  def clean[F[_]](config: PostgreSQLConfig)(implicit F: Sync[F]): F[Unit] =
    F.delay(loadFlyway(config).clean()).void
  // $COVERAGE-ON$

  private def loadFlyway(config: PostgreSQLConfig): Flyway =
    Flyway
      .configure()
      .locations("db.migration")
      .cleanDisabled(false)
      .dataSource(config.url, config.user, config.password)
      .load()
}

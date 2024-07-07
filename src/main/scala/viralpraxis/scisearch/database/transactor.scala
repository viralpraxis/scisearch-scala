package viralpraxis.scisearch.database

import cats.effect.{Async, Resource}
import cats.implicits.catsSyntaxOptionId

import doobie.hikari.{Config, HikariTransactor}
import doobie.util.ExecutionContexts

import viralpraxis.scisearch.config.PostgreSQLConfig

/** Transactors factory. */
object transactor {
  def makeTransactor[F[_]: Async](conf: PostgreSQLConfig): Resource[F, HikariTransactor[F]] = {
    val hikariConfig = Config(
      jdbcUrl = Some(conf.url),
      username = conf.user.some,
      password = conf.password.some,
      maximumPoolSize = conf.poolSize.some,
      driverClassName = "org.postgresql.Driver".some,
    )

    for {
      ce <- ExecutionContexts.fixedThreadPool[F](conf.poolSize)
      xa <- HikariTransactor.fromConfig[F](hikariConfig, ce)

    } yield xa
  }
}

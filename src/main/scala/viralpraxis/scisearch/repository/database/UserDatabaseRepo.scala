package viralpraxis.scisearch.repository.database

import cats.effect._
import viralpraxis.scisearch.repository.UserRepository
import viralpraxis.scisearch.domain.user.User
import viralpraxis.scisearch.config.AppConfig

import doobie._
import doobie.implicits._
import doobie.postgres.implicits._

/** Users database-backed repository. */
class UserDatabaseRepo[F[_]](config: AppConfig)(implicit xa: Transactor[F], F: Async[F])
    extends UserRepository[F] {
  override def findByCredential(email: String, password: Option[String]): F[Option[User]] =
    sql"SELECT * FROM users WHERE email = $email AND token = ${computePasswordHash(password, config.token.salt, config.token.secret)} LIMIT 1"
      .query[User]
      .option
      .transact(xa)

  override def create(user: User): F[Int] = {
    val query = sql"""
      INSERT INTO users(id, email, token, created_at, updated_at)
      VALUES (
        ${user.id},
        ${user.email},
        ${computePasswordHash(Some(user.password), config.token.salt, config.token.secret)},
        ${user.createdAt},
        ${user.updatedAt}
      )
    """

    query.update.run.transact(xa)
  }
}

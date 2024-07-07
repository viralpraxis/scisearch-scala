package viralpraxis.scisearch.repository.resident

import java.util.UUID

import cats.Functor
import cats.syntax.functor._

import viralpraxis.scisearch.common.cache.Cache
import viralpraxis.scisearch.repository.UserRepository
import viralpraxis.scisearch.domain.user.User

/** Users in-memory repository. */
class UserRepositoryInMemory[F[_]: Functor](
    cache: Cache[F, UUID, User],
) extends UserRepository[F] {
  override def findByCredential(email: String, password: Option[String]): F[Option[User]] =
    cache.values.map(users => users.find(user => user.email == email))
  override def create(user: User): F[Int] =
    cache
      .add(user.id, user)
      .as(0)
}

object UserRepositoryInMemory {
  def apply[F[_]: Functor](cache: Cache[F, UUID, User]) =
    new UserRepositoryInMemory[F](cache)
}

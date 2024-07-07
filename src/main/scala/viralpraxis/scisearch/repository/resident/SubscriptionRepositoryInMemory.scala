package viralpraxis.scisearch.repository.resident

import java.util.UUID

import cats.Functor
import cats.syntax.functor._

import viralpraxis.scisearch.common.cache.Cache
import viralpraxis.scisearch.domain.subscription.Subscription
import viralpraxis.scisearch.domain.user.User
import viralpraxis.scisearch.repository.SubscriptionRepository

/** Bookmarks in-memory repository. */
class SubscriptionRepositoryInMemory[F[_]: Functor](
    cache: Cache[F, UUID, Subscription],
) extends SubscriptionRepository[F] {
  override def create(user: User, subscription: Subscription): F[Int] =
    cache
      .add(subscription.id, subscription)
      .as(0)

  override def list(user: User): F[List[Subscription]] =
    cache.values.map(users => users.filter(_.userId == user.id))

  override def get(user: User, id: UUID): F[Option[Subscription]] =
    cache.get(id)
}

object SubscriptionRepositoryInMemory {
  def apply[F[_]: Functor](cache: Cache[F, UUID, Subscription]) =
    new SubscriptionRepositoryInMemory[F](cache)
}

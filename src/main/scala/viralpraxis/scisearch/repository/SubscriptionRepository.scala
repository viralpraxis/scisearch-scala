package viralpraxis.scisearch.repository

import viralpraxis.scisearch.domain.subscription.Subscription
import viralpraxis.scisearch.domain.user.User

import java.util.UUID

/** Subscriptions repository trait. */
trait SubscriptionRepository[F[_]] {
  def create(user: User, subscription: Subscription): F[Int]

  def list(user: User): F[List[Subscription]]

  def get(user: User, id: UUID): F[Option[Subscription]]
}

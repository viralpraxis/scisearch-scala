package viralpraxis.scisearch.repository.database

import cats.effect._

import java.util.UUID

import doobie._
import doobie.implicits._
import doobie.postgres.implicits._

import viralpraxis.scisearch.domain.subscription.Subscription
import viralpraxis.scisearch.repository.SubscriptionRepository
import viralpraxis.scisearch.domain.user.User

/** Subscription database-backed repository. */
class SubscriptionDatabaseRepo[F[_]](implicit xa: Transactor[F], F: Async[F])
    extends SubscriptionRepository[F] {
  override def create(user: User, subscription: Subscription): F[Int] = {
    val query = sql"""
      INSERT INTO subscriptions(id, keyword, notification_period_in_days, created_at, updated_at, user_id)
      VALUES (
        ${subscription.id},
        ${subscription.keyword},
        ${subscription.notificationPeriodInDays},
        ${subscription.createdAt},
        ${subscription.updatedAt},
        ${user.id}
      );
    """

    query.update.run.transact(xa)
  }

  override def list(user: User): F[List[Subscription]] =
    sql"SELECT * FROM subscriptions WHERE user_id = ${user.id}"
      .query[Subscription]
      .to[List]
      .transact(xa)

  override def get(user: User, id: UUID): F[Option[Subscription]] =
    sql"SELECT * FROM subscriptions WHERE id = $id AND user_id = ${user.id}"
      .query[Subscription]
      .option
      .transact(xa)
}

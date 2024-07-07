package viralpraxis.scisearch.service

import cats.FlatMap
import cats.effect.kernel.Clock
import cats.effect.std.UUIDGen
import cats.syntax.flatMap._
import cats.syntax.functor._

import viralpraxis.scisearch.domain.subscription.{Subscription, SubscriptionResponse}
import viralpraxis.scisearch.repository.SubscriptionRepository
import viralpraxis.scisearch.domain.subscription.CreateSubscription
import viralpraxis.scisearch.domain.user.User

import java.util.UUID

trait SubscriptionService[F[_]] {
  def create(user: User, subscription: CreateSubscription): F[SubscriptionResponse]

  def list(user: User): F[List[SubscriptionResponse]]

  def get(user: User, id: UUID): F[Option[SubscriptionResponse]]
}

case class RepositorySubscriptionService[F[_]: UUIDGen: FlatMap: Clock](
    subscriptionRepository: SubscriptionRepository[F],
) extends SubscriptionService[F] {
  override def create(user: User, createSubscription: CreateSubscription): F[SubscriptionResponse] =
    for {
      id <- UUIDGen[F].randomUUID
      now <- Clock[F].realTimeInstant
      subscription = Subscription(
        id,
        createSubscription.keyword,
        createSubscription.notificationPeriodInDays,
        now,
        now,
        None,
        id,
      )
      _ <- subscriptionRepository.create(user, subscription)
    } yield subscription.toResponse

  override def list(user: User): F[List[SubscriptionResponse]] =
    subscriptionRepository
      .list(user)
      .map(_.map(_.toResponse))

  override def get(user: User, id: UUID): F[Option[SubscriptionResponse]] =
    subscriptionRepository.get(user, id).map(_.map(_.toResponse))
}

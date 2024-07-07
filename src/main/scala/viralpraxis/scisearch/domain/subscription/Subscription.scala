package viralpraxis.scisearch.domain.subscription

import java.time.Instant
import java.util.UUID

/** This class represents a subscription.
  */
case class Subscription(
    id: UUID,
    keyword: String,
    notificationPeriodInDays: Int,
    createdAt: Instant,
    updatedAt: Instant,
    lastCheckedAt: Option[Instant],
    userId: UUID,
) {
  def toResponse: SubscriptionResponse =
    SubscriptionResponse(
      id = id,
      keyword = keyword,
      notificationPeriodInDays = notificationPeriodInDays,
      createdAt = createdAt,
      updatedAt = updatedAt,
      lastCheckedAt = lastCheckedAt,
      userId = userId,
    )
}

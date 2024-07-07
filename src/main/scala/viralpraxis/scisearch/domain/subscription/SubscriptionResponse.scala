package viralpraxis.scisearch.domain.subscription

import viralpraxis.scisearch.common.tethys.TethysInstances
import sttp.tapir.Schema
import sttp.tapir.Schema.annotations._
import tethys.derivation.semiauto._
import tethys.{JsonReader, JsonWriter}

import java.time.Instant
import java.util.UUID

/** This class represents a subscription response. See
  * [[viralpraxis.scisearch.domain.subscription.Subscription]] for details.
  */
final case class SubscriptionResponse(
    @description("Subscription ID")
    id: UUID,
    @description("The keyword subscription is applied to")
    keyword: String,
    @description("Notification period (in days)")
    notificationPeriodInDays: Int,
    @description("Subscription creation timestamp")
    createdAt: Instant,
    @description("Subscription last updation timestamp")
    updatedAt: Instant,
    @description("Subscription last checked timestamp")
    lastCheckedAt: Option[Instant],
    @description("Owner ID")
    userId: UUID,
)

object SubscriptionResponse extends TethysInstances {
  implicit val subscriptionResponseReader: JsonReader[SubscriptionResponse] = jsonReader

  implicit val subscriptionResponseWriter: JsonWriter[SubscriptionResponse] = jsonWriter

  implicit val subscriptionResponseSchema: Schema[SubscriptionResponse] = Schema.derived
    .description("Subscripriotn")
}

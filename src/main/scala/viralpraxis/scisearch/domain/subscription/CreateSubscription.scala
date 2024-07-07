package viralpraxis.scisearch.domain.subscription

import viralpraxis.scisearch.common.tethys.TethysInstances
import sttp.tapir.Schema
import sttp.tapir.Schema.annotations._
import tethys.derivation.semiauto._
import tethys.{JsonReader, JsonWriter}

/** This class represents a subscription creation command. See
  * [[viralpraxis.scisearch.domain.subscription.Subscription]] for details.
  */
case class CreateSubscription(
    @description("Keyword the subscription is dedicated to")
    keyword: String,
    @description("Period in days to send notifications")
    notificationPeriodInDays: Int,
)

object CreateSubscription extends TethysInstances {
  implicit val createSubscriptionReader: JsonReader[CreateSubscription] = jsonReader

  implicit val createSubscriptionWriter: JsonWriter[CreateSubscription] = jsonWriter

  implicit val createSubscriptionSchema: Schema[CreateSubscription] = Schema.derived
    .description("Запрос создания подписки")
}

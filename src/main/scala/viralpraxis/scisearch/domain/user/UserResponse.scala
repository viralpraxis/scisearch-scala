package viralpraxis.scisearch.domain.user

import java.time.Instant
import java.util.UUID

import sttp.tapir.Schema
import sttp.tapir.Schema.annotations._
import tethys.derivation.semiauto._
import tethys.{JsonReader, JsonWriter}

import viralpraxis.scisearch.common.tethys.TethysInstances

/** This class represents a user response command. See [[viralpraxis.scisearch.domain.user.User]].
  */
final case class UserResponse(
    @description("User ID")
    id: UUID,
    @description("User email")
    email: String,
    @description("User creation timestamp")
    createdAt: Instant,
    @description("User last updation timestamp")
    updatedAt: Instant,
)

object UserResponse extends TethysInstances {
  implicit val userResponseReader: JsonReader[UserResponse] = jsonReader

  implicit val userResponseWriter: JsonWriter[UserResponse] = jsonWriter

  implicit val userResponseSchema: Schema[UserResponse] = Schema.derived
    .description("User")
}

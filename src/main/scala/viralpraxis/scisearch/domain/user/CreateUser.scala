package viralpraxis.scisearch.domain.user

import viralpraxis.scisearch.common.tethys.TethysInstances
import sttp.tapir.Schema
import sttp.tapir.Schema.annotations._
import tethys.derivation.semiauto._
import tethys.{JsonReader, JsonWriter}

/** This class represents a use creation command. See [[viralpraxis.scisearch.domain.user.User]] for
  * details.
  */
case class CreateUser(
    @description("User email address")
    email: String,
    @description("User password")
    password: String,
)

object CreateUser extends TethysInstances {
  implicit val createUserReader: JsonReader[CreateUser] = jsonReader

  implicit val createUserWriter: JsonWriter[CreateUser] = jsonWriter

  implicit val createUserSchema: Schema[CreateUser] = Schema.derived
    .description("User creation request schema")
}

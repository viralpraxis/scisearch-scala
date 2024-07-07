package viralpraxis.scisearch.domain.paper

import viralpraxis.scisearch.common.tethys.TethysInstances
import sttp.tapir.Schema
import sttp.tapir.Schema.annotations._
import tethys.derivation.semiauto._
import tethys.{JsonReader, JsonWriter}

/** This class represents a bookmark. See [[viralpraxis.scisearch.domain.paper.Paper]].
  */
final case class PaperResponse(
    @description("Paper ID")
    id: String,
    @description("Paper title")
    title: String,
    @description("Last paper updation time")
    updated: String,
)

object PaperResponse extends TethysInstances {
  implicit val paperResponseReader: JsonReader[PaperResponse] = jsonReader

  implicit val paperResponseWriter: JsonWriter[PaperResponse] = jsonWriter

  implicit val paperResponseSchema: Schema[PaperResponse] = Schema.derived
    .description("Paper response schema")
}

package viralpraxis.scisearch.domain.paper

import cats.syntax.all._

import com.lucidchart.open.xtract.{XmlReader, __}
import com.lucidchart.open.xtract.XmlReader._

import sttp.tapir.Schema
import sttp.tapir.Schema.annotations._

import tethys.derivation.semiauto._
import tethys.{JsonReader, JsonWriter}

/** This class represents an `arxiv`-backed paper.
  */

case class Paper(
    @description("Paper external ID")
    id: String,
    @description("Paper title")
    title: String,
    @description("Paper updation timestamp")
    updated: String,
) {
  def toResponse: PaperResponse =
    PaperResponse(id = id, title = title, updated = updated)
}

object Paper {
  implicit val reader: XmlReader[Paper] = (
    (__ \ "id").read[String],
    (__ \ "title").read[String],
    (__ \ "updated").read[String],
  ).mapN(apply _)

  implicit val paperReader: JsonReader[Paper] = jsonReader

  implicit val paperWriter: JsonWriter[Paper] = jsonWriter

  implicit val paperSchema: Schema[Paper] = Schema.derived
    .description("Paper")
}

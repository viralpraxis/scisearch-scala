package viralpraxis.scisearch.domain.bookmark

import viralpraxis.scisearch.common.tethys.TethysInstances
import sttp.tapir.Schema
import tethys.derivation.semiauto._
import tethys.{JsonReader, JsonWriter}

/** This class represents a bookmark creation command. See
  * [[viralpraxis.scisearch.domain.bookmark.Bookmark]] for details.
  */
case class CreateBookmark(identifier: String, comment: Option[String])

object CreateBookmark extends TethysInstances {
  implicit val createBookmarkReader: JsonReader[CreateBookmark] = jsonReader

  implicit val createBookmarkWriter: JsonWriter[CreateBookmark] = jsonWriter

  implicit val createBookmarkSchema: Schema[CreateBookmark] = Schema.derived
    .description("Bookmark creation request schema")
}

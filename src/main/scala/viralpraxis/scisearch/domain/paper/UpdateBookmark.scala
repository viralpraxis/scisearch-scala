package viralpraxis.scisearch.domain.bookmark

import viralpraxis.scisearch.common.tethys.TethysInstances
import sttp.tapir.Schema
import tethys.derivation.semiauto._
import tethys.{JsonReader, JsonWriter}

/** This class represents a bookmark updation command. See
  * [[viralpraxis.scisearch.domain.bookmark.Bookmark]] for details.
  */
case class UpdateBookmark(comment: Option[String])

object UpdateBookmark extends TethysInstances {
  implicit val updateBookmarkReader: JsonReader[UpdateBookmark] = jsonReader

  implicit val updateBookmarkWriter: JsonWriter[UpdateBookmark] = jsonWriter

  implicit val updateBookmarkSchema: Schema[UpdateBookmark] = Schema.derived
    .description("Bookmark updation request model")
}

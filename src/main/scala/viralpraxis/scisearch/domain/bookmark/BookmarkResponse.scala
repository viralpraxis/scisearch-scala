package viralpraxis.scisearch.domain.bookmark

import viralpraxis.scisearch.common.tethys.TethysInstances
import sttp.tapir.Schema
import sttp.tapir.Schema.annotations._
import tethys.derivation.semiauto._
import tethys.{JsonReader, JsonWriter}

import java.time.Instant
import java.util.UUID

/** This class represents a bookmark response. See
  * [[viralpraxis.scisearch.domain.bookmark.Bookmark]] for details.
  */
final case class BookmarkResponse(
    @description("Bookmark ID")
    id: UUID,
    @description("Bookmark external identifier")
    identifier: String,
    @description("Bookmark optional user-provided comment")
    comment: Option[String],
    @description("Bookmark creation timestamp")
    createdAt: Instant,
    @description("Bookmark last updation timestamp")
    updatedAt: Instant,
    @description("Owner ID")
    userId: UUID,
)

object BookmarkResponse extends TethysInstances {
  implicit val paperResponseReader: JsonReader[BookmarkResponse] = jsonReader

  implicit val paperResponseWriter: JsonWriter[BookmarkResponse] = jsonWriter

  implicit val paperResponseSchema: Schema[BookmarkResponse] = Schema.derived
    .description("Закладка")
}

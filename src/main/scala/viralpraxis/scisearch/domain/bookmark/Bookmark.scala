package viralpraxis.scisearch.domain.bookmark

import java.time.Instant
import java.util.UUID

/** This class represents a bookmark.
  */
case class Bookmark(
    id: UUID,
    identifier: String,
    comment: Option[String],
    createdAt: Instant,
    updatedAt: Instant,
    userId: UUID,
) {
  def toResponse: BookmarkResponse =
    BookmarkResponse(
      id = id,
      identifier = identifier,
      comment = comment,
      createdAt = createdAt,
      updatedAt = updatedAt,
      userId = userId,
    )
}

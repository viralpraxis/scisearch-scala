package viralpraxis.scisearch.repository

import viralpraxis.scisearch.domain.bookmark.{Bookmark, UpdateBookmark}
import viralpraxis.scisearch.domain.user.User

import java.util.UUID

/** Bookmarks repository trait. */
trait BookmarkRepository[F[_]] {
  def create(user: User, bookmark: Bookmark): F[Int]

  def update(user: User, id: UUID, bookmark: UpdateBookmark): F[Int]

  def list(user: User): F[List[Bookmark]]

  def get(user: User, id: UUID): F[Option[Bookmark]]

  def delete(user: User, id: UUID): F[Int]
}

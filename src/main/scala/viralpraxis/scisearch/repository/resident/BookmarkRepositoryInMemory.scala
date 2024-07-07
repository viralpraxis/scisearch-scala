package viralpraxis.scisearch.repository.resident

import java.util.UUID

import cats.Functor
import cats.syntax.functor._

import viralpraxis.scisearch.common.cache.Cache
import viralpraxis.scisearch.domain.bookmark.{Bookmark, UpdateBookmark}
import viralpraxis.scisearch.repository.BookmarkRepository
import viralpraxis.scisearch.domain.user.User

/** Bookmarks in-memory repository. */
class BookmarkRepositoryInMemory[F[_]: Functor](
    cache: Cache[F, UUID, Bookmark],
) extends BookmarkRepository[F] {
  override def create(user: User, bookmark: Bookmark): F[Int] =
    cache
      .add(bookmark.id, bookmark)
      .as(0)

  override def list(user: User): F[List[Bookmark]] =
    cache.values.map(users => users.filter(_.userId == user.id))

  override def get(user: User, id: UUID): F[Option[Bookmark]] =
    cache.get(id)

  override def delete(user: User, id: UUID): F[Int] =
    Functor[F].ifF(cache.hasKey(id))({ cache.remove(id); 1 }, 0)

  override def update(user: User, id: UUID, bookmark: UpdateBookmark): F[Int] =
    Functor[F].ifF(cache.hasKey(id))(
      {
        cache
          .get(id)
          .map(_.map(bookmark => bookmark.copy(comment = bookmark.comment)))
          .map(_ => cache.add(id, _))
        1
      },
      0,
    )

}

object BookmarkRepositoryInMemory {
  def apply[F[_]: Functor](cache: Cache[F, UUID, Bookmark]) =
    new BookmarkRepositoryInMemory[F](cache)
}

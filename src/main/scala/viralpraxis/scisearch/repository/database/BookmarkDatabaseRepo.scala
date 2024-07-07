package viralpraxis.scisearch.repository.database

import cats.effect._

import java.util.UUID

import doobie._
import doobie.implicits._
import doobie.postgres.implicits._

import viralpraxis.scisearch.domain.bookmark.{Bookmark, UpdateBookmark}
import viralpraxis.scisearch.repository.BookmarkRepository
import viralpraxis.scisearch.domain.user.User

/** Bookmark database-backed repository. */
class BookmarkDatabaseRepo[F[_]](implicit xa: Transactor[F], F: Async[F])
    extends BookmarkRepository[F] {
  override def list(user: User): F[List[Bookmark]] =
    sql"SELECT id, identifier, comment, created_at, updated_at, user_id FROM bookmarks WHERE user_id = ${user.id}"
      .query[Bookmark]
      .to[List]
      .transact(xa)

  override def create(user: User, bookmark: Bookmark): F[Int] = {
    val query = sql"""
      INSERT INTO bookmarks(id, identifier, comment, created_at, updated_at, user_id)
      VALUES (
        ${bookmark.id},
        ${bookmark.identifier},
        ${bookmark.comment},
        ${bookmark.createdAt},
        ${bookmark.updatedAt},
        ${user.id}
      );
    """

    query.update.run.transact(xa)
  }

  override def update(user: User, id: UUID, bookmark: UpdateBookmark): F[Int] = {
    val query = sql"""
      UPDATE bookmarks SET
        comment = ${bookmark.comment}
      WHERE id = $id AND user_id = ${user.id};
    """

    query.update.run
      .transact(xa)
  }

  override def get(user: User, id: UUID): F[Option[Bookmark]] =
    sql"SELECT id, identifier, comment, created_at, updated_at, user_id FROM bookmarks WHERE id = $id AND user_id = ${user.id}"
      .query[Bookmark]
      .option
      .transact(xa)

  override def delete(user: User, id: UUID): F[Int] =
    sql"DELETE FROM bookmarks WHERE id = $id AND user_id = ${user.id}".update.run
      .transact(xa)
}

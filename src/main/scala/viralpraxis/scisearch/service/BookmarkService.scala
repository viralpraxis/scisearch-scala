package viralpraxis.scisearch.service

import cats.FlatMap
import cats.effect.kernel.Clock
import cats.effect.std.UUIDGen
import cats.syntax.flatMap._
import cats.syntax.functor._

import viralpraxis.scisearch.domain.bookmark.{Bookmark, BookmarkResponse, UpdateBookmark}
import viralpraxis.scisearch.repository.BookmarkRepository
import viralpraxis.scisearch.domain.bookmark.CreateBookmark
import viralpraxis.scisearch.domain.user.User

import java.util.UUID

trait BookmarkService[F[_]] {
  def create(user: User, bookmark: CreateBookmark): F[BookmarkResponse]

  def update(user: User, id: UUID, updateBookmark: UpdateBookmark): F[Int]

  def list(user: User): F[List[BookmarkResponse]]

  def get(user: User, id: UUID): F[Option[BookmarkResponse]]

  def delete(user: User, id: UUID): F[Int]
}

case class RepositoryBookmarkService[F[_]: UUIDGen: FlatMap: Clock](
    bookmarkRepository: BookmarkRepository[F],
) extends BookmarkService[F] {
  override def create(user: User, createBookmark: CreateBookmark): F[BookmarkResponse] =
    for {
      id <- UUIDGen[F].randomUUID
      now <- Clock[F].realTimeInstant
      bookmark = Bookmark(id, createBookmark.identifier, createBookmark.comment, now, now, user.id)
      _ <- bookmarkRepository.create(user, bookmark)
    } yield bookmark.toResponse

  override def update(
      user: User,
      id: UUID,
      updateBookmark: UpdateBookmark,
  ): F[Int] =
    bookmarkRepository.update(user, id, updateBookmark)

  override def list(user: User): F[List[BookmarkResponse]] =
    bookmarkRepository
      .list(user)
      .map(_.map(_.toResponse))

  override def get(user: User, id: UUID): F[Option[BookmarkResponse]] =
    bookmarkRepository.get(user, id).map(_.map(_.toResponse))

  override def delete(user: User, id: UUID): F[Int] =
    bookmarkRepository.delete(user, id)
}

package viralpraxis.scisearch.service

import cats.effect.kernel.Clock
import cats.effect.std.UUIDGen
import cats.syntax.flatMap._
import cats.syntax.functor._

import viralpraxis.scisearch.repository.UserRepository
import viralpraxis.scisearch.domain.user.{CreateUser, User, UserResponse}
import cats.FlatMap

trait UserService[F[_]] {
  def findByCredential(email: String, password: Option[String]): F[Option[User]]

  def create(createUser: CreateUser): F[UserResponse]
}

case class RepositoryUserService[F[_]: UUIDGen: FlatMap: Clock](
    userRepository: UserRepository[F],
) extends UserService[F] {
  override def findByCredential(email: String, password: Option[String]): F[Option[User]] =
    userRepository.findByCredential(email, password)

  override def create(createUser: CreateUser): F[UserResponse] =
    for {
      id <- UUIDGen[F].randomUUID
      now <- Clock[F].realTimeInstant
      user = User(id, createUser.email, createUser.password, now, now)
      _ <- userRepository.create(user)
    } yield user.toResponse
}

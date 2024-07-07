package viralpraxis.scisearch.repository

import com.roundeights.hasher.Implicits._

import viralpraxis.scisearch.domain.user.User

/** Users repository trait. */
trait UserRepository[F[_]] {
  def findByCredential(email: String, password: Option[String]): F[Option[User]]

  def create(user: User): F[Int]

  protected def computePasswordHash(
      password: Option[String],
      salt: String,
      secret: String,
  ): String =
    password match {
      case None        => ""
      case Some(value) => value.salt(salt).pbkdf2(secret, 10, 256).hex
    }
}

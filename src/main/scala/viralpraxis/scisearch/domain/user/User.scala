package viralpraxis.scisearch.domain.user

import java.time.Instant
import java.util.UUID

case class User(
    id: UUID,
    email: String,
    password: String,
    createdAt: Instant,
    updatedAt: Instant,
) {
  def toResponse: UserResponse =
    UserResponse(
      id = id,
      email = email,
      createdAt = createdAt,
      updatedAt = updatedAt,
    )
}

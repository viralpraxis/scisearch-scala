package viralpraxis.scisearch.controller

import sttp.tapir.json.tethysjson.jsonBody
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.generic.auto._
import sttp.tapir._

import viralpraxis.scisearch.common.controller.Controller
import viralpraxis.scisearch.domain.user.{CreateUser, UserResponse}
import viralpraxis.scisearch.service.UserService

/** Controller for users-related endpoints. */
class UsersController[F[_]](userService: UserService[F]) extends Controller[F] {
  val baseEndpoint = endpoint
    .in("api" / "v1")

  val createUser: ServerEndpoint[Any, F] =
    baseEndpoint.post
      .summary("Create user")
      .in("users")
      .in(jsonBody[CreateUser])
      .out(jsonBody[UserResponse])
      .serverLogicSuccess(userService.create(_))

  override val endpoints: List[ServerEndpoint[Any, F]] =
    List(createUser).map(_.withTag("Users"))
}

object UsersController {
  def make[F[_]](userService: UserService[F]): UsersController[F] =
    new UsersController[F](userService)
}

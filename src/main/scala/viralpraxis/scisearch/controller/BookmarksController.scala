package viralpraxis.scisearch.controller

import java.util.UUID

import sttp.tapir.json.tethysjson.jsonBody
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.PartialServerEndpoint
import sttp.tapir.generic.auto._
import sttp.tapir._

import cats.Functor
import cats.data.EitherT

import viralpraxis.scisearch.common.controller.Controller
import viralpraxis.scisearch.domain.bookmark.{BookmarkResponse, CreateBookmark, UpdateBookmark}
import viralpraxis.scisearch.domain.user.User
import viralpraxis.scisearch.service.{BookmarkService, UserService}
import sttp.model.StatusCode
import sttp.tapir.model.UsernamePassword

/** Controller for bookmarks-related endpoints. */
class BookmarksController[F[_]: Functor](
    bookmarkService: BookmarkService[F],
    userService: UserService[F],
) extends Controller[F] {
  import viralpraxis.scisearch.controller.ProblemDetails._

  def authLogic(credential: UsernamePassword): F[Either[GenericError, User]] =
    EitherT
      .fromOptionF[F, GenericError, User](
        userService.findByCredential(credential.username, credential.password),
        new GenericError("Unauthorized"),
      )
      .value

  val secureEndpoint
      : PartialServerEndpoint[UsernamePassword, User, Unit, GenericError, Unit, Any, F] =
    endpoint
      .securityIn(auth.basic[UsernamePassword]())
      .errorOut(statusCode(StatusCode.BadRequest).and(jsonBody[GenericError]))
      .serverSecurityLogic(authLogic)

  val createBookmarks: ServerEndpoint[Any, F] =
    secureEndpoint.post
      .summary("Create bookmark")
      .in("api" / "v1" / "bookmarks")
      .in(jsonBody[CreateBookmark])
      .out(jsonBody[BookmarkResponse])
      .serverLogicSuccess { (user: User) => (create: CreateBookmark) =>
        bookmarkService.create(user, create)
      }

  val updateBookmarks: ServerEndpoint[Any, F] =
    secureEndpoint.patch
      .summary("Update bookmark")
      .in("api" / "v1" / "bookmarks" / path[UUID]("id"))
      .in(jsonBody[UpdateBookmark])
      .out(statusCode(StatusCode.NoContent).and(emptyOutput))
      .serverLogic((user: User) => { case (id: UUID, update: UpdateBookmark) =>
        Functor[F].map(bookmarkService.update(user, id, update))(result =>
          if (result > 0) {
            Right()
          } else {
            Left(new GenericError("Not Found"))
          },
        )
      })

  val listBookmarks: ServerEndpoint[Any, F] =
    secureEndpoint.get
      .summary("List bookmarks")
      .in("api" / "v1" / "bookmarks")
      .out(jsonBody[List[BookmarkResponse]])
      .serverLogicSuccess((user: User) => (_: Unit) => bookmarkService.list(user))

  val getBookmark: ServerEndpoint[Any, F] =
    secureEndpoint.get
      .summary("Find bookmark by ID")
      .in("api" / "v1" / "bookmarks" / path[UUID]("id"))
      .out(jsonBody[BookmarkResponse])
      .serverLogic((user: User) =>
        (id: UUID) =>
          EitherT
            .fromOptionF[F, GenericError, BookmarkResponse](
              bookmarkService.get(user, id),
              new GenericError("Not Found"),
            )
            .value,
      )

  val deleteBookmark: ServerEndpoint[Any, F] =
    secureEndpoint.delete
      .summary("Delete bookmark")
      .in("api" / "v1" / "bookmarks" / path[UUID]("id"))
      .out(statusCode(StatusCode.NoContent).and(emptyOutput))
      .serverLogic((user: User) =>
        (id: UUID) =>
          Functor[F].map(bookmarkService.delete(user, id))(result =>
            if (result > 0) {
              Right()
            } else {
              Left(new GenericError("Not Found"))
            },
          ),
      )

  override val endpoints: List[ServerEndpoint[Any, F]] =
    List(
      createBookmarks,
      updateBookmarks,
      listBookmarks,
      getBookmark,
      deleteBookmark,
    ).map(_.withTag("Bookmarks"))
}

object BookmarksController {
  def make[F[_]: Functor](
      bookmarkService: BookmarkService[F],
      userService: UserService[F],
  ): BookmarksController[F] =
    new BookmarksController[F](bookmarkService, userService)
}

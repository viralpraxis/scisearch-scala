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
import viralpraxis.scisearch.domain.subscription.{CreateSubscription, SubscriptionResponse}
import viralpraxis.scisearch.domain.user.User
import viralpraxis.scisearch.service.{SubscriptionService, UserService}
import sttp.model.StatusCode
import sttp.tapir.model.UsernamePassword

/** Controller for subscriptions-related endpoints. */
class SubscriptionsController[F[_]: Functor](
    subscriptionService: SubscriptionService[F],
    userService: UserService[F],
) extends Controller[F] {
  import viralpraxis.scisearch.controller.ProblemDetails._

  def authLogic(credential: UsernamePassword): F[Either[GenericError, User]] =
    EitherT
      .fromOptionF[F, GenericError, User](
        userService.findByCredential(credential.username, credential.password),
        new GenericError("Invalid auth token"),
      )
      .value

  val secureEndpoint
      : PartialServerEndpoint[UsernamePassword, User, Unit, GenericError, Unit, Any, F] =
    endpoint
      .securityIn(auth.basic[UsernamePassword]())
      .errorOut(statusCode(StatusCode.Unauthorized).and(jsonBody[GenericError]))
      .serverSecurityLogic(authLogic)

  val createSubscriptions: ServerEndpoint[Any, F] =
    secureEndpoint.post
      .summary("Create subscription")
      .in("api" / "v1" / "subscriptions")
      .in(jsonBody[CreateSubscription])
      .errorOutVariant(
        oneOfVariant(statusCode(StatusCode.UnprocessableEntity).and(jsonBody[GenericError])),
      )
      .out(jsonBody[SubscriptionResponse])
      .serverLogicSuccess { (user: User) => (create: CreateSubscription) =>
        subscriptionService.create(user, create)
      }

  val listSubscriptions: ServerEndpoint[Any, F] =
    secureEndpoint.get
      .summary("List subscriptions")
      .in("api" / "v1" / "subscriptions")
      .errorOutVariant(
        oneOfVariant(statusCode(StatusCode.UnprocessableEntity).and(jsonBody[GenericError])),
      )
      .out(jsonBody[List[SubscriptionResponse]])
      .serverLogicSuccess((user: User) => (_: Unit) => subscriptionService.list(user))

  val getSubscription: ServerEndpoint[Any, F] =
    secureEndpoint.get
      .summary("Find subscription by ID")
      .in("api" / "v1" / "subscriptions" / path[UUID]("id"))
      .errorOutVariant(
        oneOfVariant(statusCode(StatusCode.UnprocessableEntity).and(jsonBody[GenericError])),
      )
      .out(jsonBody[SubscriptionResponse])
      .serverLogic((user: User) =>
        (id: UUID) =>
          EitherT
            .fromOptionF[F, GenericError, SubscriptionResponse](
              subscriptionService.get(user, id),
              GenericError("Not found"),
            )
            .value,
      )

  override val endpoints: List[ServerEndpoint[Any, F]] =
    List(
      createSubscriptions,
      listSubscriptions,
      getSubscription,
    ).map(_.withTag("Subscriptions"))
}

object SubscriptionsController {
  def make[F[_]: Functor](
      subscriptionService: SubscriptionService[F],
      userService: UserService[F],
  ): SubscriptionsController[F] =
    new SubscriptionsController[F](subscriptionService, userService)
}

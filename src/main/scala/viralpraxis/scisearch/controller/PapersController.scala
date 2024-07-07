package viralpraxis.scisearch.controller

import viralpraxis.scisearch.common.controller.Controller
import viralpraxis.scisearch.domain.paper.PaperResponse
import viralpraxis.scisearch.service.PaperService
import sttp.tapir.json.tethysjson.jsonBody
import sttp.tapir.server.ServerEndpoint
import sttp.tapir._

/** Controller for papers-related endpoints. */
class PapersController[F[_]](paperService: PaperService[F]) extends Controller[F] {
  val listPapers: ServerEndpoint[Any, F] =
    endpoint.get
      .summary("Список статей")
      .in("api" / "v1" / "papers")
      .in(query[String]("keyword"))
      .out(jsonBody[List[PaperResponse]])
      .serverLogicSuccess(paperService.list(_))

  override val endpoints: List[ServerEndpoint[Any, F]] =
    List(listPapers)
      .map(_.withTag("Papers"))
}

object PapersController {
  def make[F[_]](paperService: PaperService[F]): PapersController[F] =
    new PapersController[F](paperService)
}

package viralpraxis.scisearch.repository.resident

import cats.Functor
import cats.syntax.functor._
import viralpraxis.scisearch.domain.paper.Paper
import viralpraxis.scisearch.repository.PaperRepository
import viralpraxis.scisearch.service.ArxivClient

/** Papers API-backed repository. */
class PaperDatabaseRepo[F[_]: Functor](arxivClient: ArxivClient[F]) extends PaperRepository[F] {
  override def list(keyword: String): F[List[Paper]] = arxivClient
    .findPapers(keyword)
    .map(x =>
      x match {
        case Left(_)     => List[Paper]()
        case Right(feed) => feed.papers.toList
      },
    )
}

object PaperDatabaseRepo {
  def apply[F[_]: Functor](arxivClient: ArxivClient[F]) =
    new PaperDatabaseRepo[F](arxivClient)
}

package viralpraxis.scisearch.service

import cats.FlatMap
import cats.syntax.functor._
import viralpraxis.scisearch.domain.paper.PaperResponse
import viralpraxis.scisearch.repository.PaperRepository

trait PaperService[F[_]] {
  def list(keyword: String): F[List[PaperResponse]]
}

case class RepositoryPaperService[F[_]: FlatMap](
    paperRepository: PaperRepository[F],
) extends PaperService[F] {
  override def list(keyword: String): F[List[PaperResponse]] =
    paperRepository
      .list(keyword)
      .map(_.map(_.toResponse))
}

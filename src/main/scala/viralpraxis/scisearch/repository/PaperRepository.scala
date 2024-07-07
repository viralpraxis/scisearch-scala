package viralpraxis.scisearch.repository

import viralpraxis.scisearch.domain.paper.Paper

/** Papers repository trait. */
trait PaperRepository[F[_]] {
  def list(keyword: String): F[List[Paper]]
}

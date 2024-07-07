package viralpraxis.scisearch.controller

import org.scalatest.EitherValues
import org.scalatest.matchers.should.Matchers
import sttp.client3.testing.SttpBackendStub
import sttp.client3.{UriContext, basicRequest}
import sttp.tapir.server.stub.TapirStubInterpreter
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import sttp.tapir.integ.cats.effect.CatsMonadError

import org.scalatest.funspec.AnyFunSpec

import viralpraxis.scisearch.domain.paper.Paper
import viralpraxis.scisearch.service.RepositoryPaperService
import viralpraxis.scisearch.repository.resident.PaperDatabaseRepo

import tethys._
import tethys.jackson._

import cats.effect.std.Random
import viralpraxis.scisearch.service.ArxivClient
import sttp.client4.testing.SyncBackendStub
import sttp.client4.SyncBackend
import sttp.model.Method

class PaperControllerSpec extends AnyFunSpec with Matchers with EitherValues {
  implicit val random: Random[IO] = Random.scalaUtilRandom[IO].unsafeRunSync()

  describe("GET /api/v1/papers") {
    it("responds with papers") {
      val responseData = scala.io.Source
        .fromFile("src/test/scala/viralpraxis/scisearch/fixtures/arxiv_client/test.xml")
        .mkString

      implicit val testingBackend: SyncBackend = SyncBackendStub
        .whenRequestMatches(x =>
          x.method == Method.GET && x.uri.path.startsWith(List("api", "query")),
        )
        .thenRespond(responseData)

      val arxivClient = ArxivClient.make[IO](testingBackend).unsafeRunSync()

      val backendStub = TapirStubInterpreter(SttpBackendStub(new CatsMonadError[IO]()))
        .whenServerEndpointRunLogic(
          PapersController
            .make(
              RepositoryPaperService(PaperDatabaseRepo(arxivClient)),
            )
            .listPapers,
        )
        .backend()

      val keyword = "optics"

      val response = basicRequest
        .get(uri"http://test.com/api/v1/papers?keyword=$keyword")
        .send(backendStub)

      val papers = response
        .unsafeRunSync()
        .body
        .value
        .jsonAs[List[Paper]]

      response.map(_.code.code shouldBe 200).unsafeRunSync()
      papers.map(_.size shouldBe 10)
      papers.map(_(0).title shouldBe "Optical frequency combs generated mechanically")

    }
  }
}

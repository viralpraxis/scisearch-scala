package viralpraxis.scisearch.service

import viralpraxis.scisearch.domain.paper._

import org.scalatest.matchers.should.Matchers

import cats.effect.IO

import sttp.client4._
import sttp.model._
import sttp.client4.testing._
import org.scalatest.funspec.AnyFunSpec
import viralpraxis.scisearch.service.ArxivClient

import cats.effect.unsafe.implicits.global

class ArxivClientSpec extends AnyFunSpec with Matchers {
  describe("#findPapers") {
    it("returns feed objects") {
      val responseData = scala.io.Source
        .fromFile("src/test/scala/viralpraxis/scisearch/fixtures/arxiv_client/test.xml")
        .mkString

      implicit val testingBackend: SyncBackend = SyncBackendStub
        .whenRequestMatches(x =>
          x.method == Method.GET && x.uri.path.startsWith(List("api", "query")),
        )
        .thenRespond(responseData)

      val result =
        ArxivClient.make[IO](testingBackend).flatMap(_.findPapers("optics")).unsafeRunSync()

      result.isRight shouldBe true
      val feed = result.right.get
      feed shouldBe a[Feed]
      feed.papers should have size 10
    }
  }

  describe("#findByIds") {
    it("returns feed with single paper for single id") {
      val responseData = scala.io.Source
        .fromFile("src/test/scala/viralpraxis/scisearch/fixtures/arxiv_client/id_list/single.xml")
        .mkString

      implicit val testingBackend = SyncBackendStub
        .whenRequestMatches(x =>
          x.method == Method.GET && x.uri.path.startsWith(List("api", "query")),
        )
        .thenRespond(responseData)

      val expectedPapersResult = List(
        Paper(
          "http://arxiv.org/abs/astro-ph/0608371v1",
          "Electron thermal conductivity owing to collisions between degenerate\n  electrons",
          "2006-08-17T14:05:46Z",
        ),
      )

      val result =
        ArxivClient
          .make[IO](testingBackend)
          .flatMap(_.findByIds(List("astro-ph/0608371v1")))
          .unsafeRunSync()

      result.isRight shouldBe true
      result.map(_.papers shouldBe expectedPapersResult)
    }

    it("returns feed with multiple papers for multiple ids") {
      val responseData = scala.io.Source
        .fromFile("src/test/scala/viralpraxis/scisearch/fixtures/arxiv_client/id_list/multiple.xml")
        .mkString

      implicit val testingBackend = SyncBackendStub
        .whenRequestMatches(x =>
          x.method == Method.GET && x.uri.path.startsWith(List("api", "query")),
        )
        .thenRespond(responseData)

      val expectedPapersResult = List(
        Paper(
          "http://arxiv.org/abs/astro-ph/0608371v1",
          "Electron thermal conductivity owing to collisions between degenerate\n  electrons",
          "2006-08-17T14:05:46Z",
        ),
        Paper(
          "http://arxiv.org/abs/1501.04914v1",
          "Hamiltonian of a many-electron system with single-electron and\n  electron-pair states in a two-dimensional periodic potential",
          "2015-01-20T18:48:22Z",
        ),
      )

      val result =
        ArxivClient
          .make[IO](testingBackend)
          .flatMap(_.findByIds(List("astro-ph/0608371v1,1501.04914v1")))
          .unsafeRunSync()

      result.isRight shouldBe true
      result.map(_.papers shouldBe expectedPapersResult)
    }
  }
}

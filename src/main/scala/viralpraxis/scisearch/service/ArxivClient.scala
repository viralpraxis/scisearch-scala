package viralpraxis.scisearch.service

import scala.xml.XML
import scala.util.{Failure, Success, Try}
import scala.annotation.tailrec

import cats.Functor
import cats.Monad
import cats.effect.Sync

import sttp.client4._

import com.lucidchart.open.xtract._
import com.lucidchart.open.xtract.{ParseResult, XmlReader}

import viralpraxis.scisearch.domain.paper.Feed
import viralpraxis.scisearch.domain.paper.Feed._

/** `Arxiv` API client trait. */
trait ArxivClientTrait[F[_]] {
  def findPapers(keyword: String): F[Either[String, Feed]]

  def findByIds(id: List[String]): F[Either[String, Feed]]
}

class ArxivClient[F[_]: Sync](val backend: SyncBackend) extends ArxivClientTrait[F] {
  private val retriesCount = 5
  private val baseURI = "https://export.arxiv.org/api"

  def findPapers(keyword: String): F[Either[String, Feed]] = {
    val doRequest = () =>
      basicRequest
        .get(uri"$baseURI/query?search_query=all:$keyword")
        .response(asXMLFeed)
        .send(backend)

    val response = Sync[F].delay(
      retry[Response[ParseResult[Feed]]](retriesCount, r => r.code.isServerError)(doRequest()),
    )

    Functor[F].map(response)(x =>
      x.body match {
        case ParseSuccess(feed)             => Right(feed)
        case ParseFailure(errors)           => Left(errors.map(_.toString).mkString)
        case PartialParseSuccess(_, errors) => Left(errors.map(_.toString).mkString)
      },
    )
  }

  def findByIds(ids: List[String]): F[Either[String, Feed]] = {
    val doRequest = () =>
      basicRequest
        .get(uri"$baseURI/query?id_list=${ids.mkString(",")}")
        .response(asXMLFeed)
        .send(backend)

    val response = Sync[F].delay(
      retry[Response[ParseResult[Feed]]](retriesCount, r => r.code.isServerError)(doRequest()),
    )

    Functor[F].map(response)(x =>
      x.body match {
        case ParseSuccess(feed)             => Right(feed)
        case ParseFailure(errors)           => Left(errors.map(_.toString).mkString)
        case PartialParseSuccess(_, errors) => Left(errors.map(_.toString).mkString)
      },
    )
  }

  private def parseXmlFeed(xml: String): ParseResult[Feed] =
    XmlReader.of[Feed].read(XML.loadString(xml))

  private val asXMLFeed: ResponseAs[ParseResult[Feed]] = asStringAlways.map(parseXmlFeed)

  @tailrec
  private def retry[T](n: Int, retryCriterion: T => Boolean)(fn: => T): T =
    Try(fn) match {
      case Success(x) if !retryCriterion(x) => x
      case Success(_)                       => retry(n - 1, retryCriterion)(fn)
      case _ if n > 1                       => retry(n - 1, retryCriterion)(fn)
      case Failure(e)                       => throw e
    }
}

object ArxivClient {
  def make[F[_]: Sync](backend: SyncBackend = DefaultSyncBackend()): F[ArxivClient[F]] =
    Monad[F].pure(new ArxivClient[F](backend))
}

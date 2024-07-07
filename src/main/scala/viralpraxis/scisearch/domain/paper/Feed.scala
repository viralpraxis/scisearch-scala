package viralpraxis.scisearch.domain.paper

import com.lucidchart.open.xtract.{XmlReader, __}
import com.lucidchart.open.xtract.XmlReader._

/** This class represents an `arxiv`-backed feed.
  */
case class Feed(papers: Seq[Paper])

object Feed {
  implicit val reader: XmlReader[Feed] = (
    (__ \ "entry").read(seq[Paper]),
  ).map(apply _)
}

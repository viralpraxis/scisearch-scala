package viralpraxis.scisearch.controller

import tethys.JsonReader
import tethys.JsonWriter

object ProblemDetails {
  sealed trait Base { def message: String; def toMap: Map[String, String] }

  final case class GenericError(message: String) extends Base {
    def toMap: Map[String, String] = Map("message" -> message)
  }

  implicit val genericErrorReader: JsonReader[GenericError] =
    JsonReader[String].map(new GenericError(_))
  implicit val genericErrorWriter: JsonWriter[GenericError] =
    JsonWriter[Map[String, String]].contramap(_.toMap)
}

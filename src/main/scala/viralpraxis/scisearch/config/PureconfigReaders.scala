package viralpraxis.scisearch.config

import pureconfig.generic.semiauto._
import pureconfig.ConfigReader

object PureconfigReaders {
  implicit val tokenConfigReader: ConfigReader[TokenConfig] = deriveReader[TokenConfig]
  implicit val pgConfigReader: ConfigReader[PostgreSQLConfig] = deriveReader[PostgreSQLConfig]
  implicit val appConfigReader: ConfigReader[AppConfig] = deriveReader[AppConfig]
}

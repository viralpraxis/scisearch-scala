package viralpraxis.scisearch.config

// https://github.com/tpolecat/doobie/blob/main/modules/hikari/src/main/scala/doobie/hikari/Config.scala
case class PostgreSQLConfig(
    host: String,
    port: Int,
    database: String,
    user: String,
    password: String,
    poolSize: Int,
) {
  lazy val url = s"jdbc:postgresql://$host:$port/$database"
}

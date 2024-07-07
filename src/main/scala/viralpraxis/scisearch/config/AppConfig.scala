package viralpraxis.scisearch.config

case class AppConfig(
    bind: String,
    port: Int,
    database: PostgreSQLConfig,
    token: TokenConfig,
)

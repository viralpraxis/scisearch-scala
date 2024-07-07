ThisBuild / version := "1.0.0"

ThisBuild / scalaVersion := "2.13.10"

val catsVersion = "2.9.0"
val catsEffect3 = "3.4.8"
val scalatestVersion = "3.2.17"
val scalamockVersion = "5.2.0"
val tapirVersion = "1.7.6"
val http4sVersion = "0.23.23"
val logbackVersion = "1.4.11"
val tethysVersion = "0.26.0"
val enumeratumVersion = "1.7.2"
val client4Version = "4.0.0-M6"
val doobieVersion = "1.0.0-RC2"
val flywayVersion = "9.16.0"
val pureConfigVersion = "0.17.4"
val bcryptVersion = "3.1"
val munitVersion = "0.7.29"
val testcontainersScalatestVersion = "0.40.12"
val testcontainersPostgresqlVersion = "0.40.12"
val ceTestingVersion = "1.5.0"

coverageFailOnMinimum := true
coverageMinimumStmtTotal := 70

scalacOptions ++= Seq(
  "-encoding",
  "utf8",
  "-Wunused",
)

lazy val root = (project in file("."))
  .settings(
    libraryDependencies ++= Seq(
      // cats
      "org.typelevel" %% "cats-core" % catsVersion,
      "org.typelevel" %% "cats-effect" % catsEffect3,

      // tapir
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-tethys" % tapirVersion,
      "com.softwaremill.sttp.client4" %% "core" % client4Version,
      "com.softwaremill.sttp.model" %% "core" % "1.7.6",
      "com.lucidchart" %% "xtract" % "2.3.0",
      "com.typesafe.play" % "play-json_2.11" % "2.6.7",

      // http4s
      "org.http4s" %% "http4s-ember-server" % http4sVersion,

      // logback
      "ch.qos.logback" % "logback-classic" % logbackVersion,

      // tethys
      "com.tethys-json" %% "tethys-core" % tethysVersion,
      "com.tethys-json" %% "tethys-jackson" % tethysVersion,
      "com.tethys-json" %% "tethys-derivation" % tethysVersion,

      // enumeratum
      "com.beachape" %% "enumeratum" % enumeratumVersion,

      // test
      "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server" % tapirVersion % Test,
      "org.scalatest" %% "scalatest" % scalatestVersion % Test,
      "org.scalamock" %% "scalamock" % scalamockVersion % Test,
      "org.scalameta" %% "munit" % munitVersion % Test,

      // doobie
      "org.tpolecat" %% "doobie-core" % doobieVersion,
      "org.tpolecat" %% "doobie-postgres" % doobieVersion,
      "org.tpolecat" %% "doobie-specs2" % doobieVersion,
      "org.tpolecat" %% "doobie-hikari" % doobieVersion,

      // flyway
      "org.flywaydb" % "flyway-core" % flywayVersion,

      // pureconfig
      "com.github.pureconfig" %% "pureconfig" % pureConfigVersion,
      "com.github.pureconfig" %% "pureconfig-cats-effect" % pureConfigVersion,

      // BCrypt
      "com.outr" %% "hasher" % "1.2.2",
      "org.mindrot" % "jbcrypt" % "0.4",

      // E2E testing
      "org.typelevel" %% "cats-effect-testing-scalatest" % ceTestingVersion % IntegrationTest,
      "com.dimafeng" %% "testcontainers-scala-scalatest" % testcontainersScalatestVersion % IntegrationTest,
      "com.dimafeng" %% "testcontainers-scala-postgresql" % testcontainersPostgresqlVersion % IntegrationTest,
    ),
    name := "scisearch",
  )
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)
  .configs(IntegrationTest)
  .settings(
    Defaults.itSettings,
    IntegrationTest / fork := true,
  )
  .settings(
    Compile / run / fork := true,
  )

dockerBaseImage := "openjdk:17-jdk-slim"
dockerExposedPorts ++= Seq(3000)

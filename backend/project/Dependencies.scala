import sbt._

object Dependencies {

  private val catsVersion = "2.7.0"
  private val catsEffectVersion = "3.3.12"
  private val circeVersion = "0.14.2"
  private val cirisVersion = "2.3.3"
  private val doobieVersion = "1.0.0-RC2"
  private val enumeratumVersion = "1.7.2"
  private val http4sVersion = "0.23.12"
  private val http4sJettyServerVersion = "0.23.10"
  private val refinedVersion = "0.10.1"
  private val scalaTestVersion = "3.2.11"
  private val tapirVersion = "1.2.1"

  private val cats = List(
    "org.typelevel" %% "cats-core"
  ).map(_ % catsVersion)

  private val catsEffect = List(
    "org.typelevel" %% "cats-effect"
  ).map(_ % catsEffectVersion)

  private val circe = List(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-refined"
  ).map(_ % circeVersion)

  private val ciris = List(
    "is.cir" %% "ciris"
  ).map(_ % cirisVersion)

  private val doobie = List(
    "org.tpolecat" %% "doobie-core",
    "org.tpolecat" %% "doobie-postgres",
    "org.tpolecat" %% "doobie-refined"
  ).map(_ % doobieVersion)

  private val enumeratum = List(
    "com.beachape" %% "enumeratum",
    "com.beachape" %% "enumeratum-circe",
    "com.beachape" %% "enumeratum-doobie"
  ).map(_ % enumeratumVersion)

  private val http4s = List(
    "org.http4s" %% "http4s-circe",
    "org.http4s" %% "http4s-core",
    "org.http4s" %% "http4s-dsl",
    "org.http4s" %% "http4s-server"
  ).map(_ % http4sVersion)

  private val http4sJettyServer = List(
    "org.http4s" % "http4s-jetty-server_2.13"
  ).map(_ % http4sJettyServerVersion)

  private val refined = List(
    "eu.timepit" %% "refined"
  ).map(_ % refinedVersion)

  private val sangria = List(
    "org.sangria-graphql" %% "sangria" % "3.4.1",
    "org.sangria-graphql" %% "sangria-circe" % "1.3.2"
  )

  private val scalaTest = List(
    "org.scalatest" %% "scalatest"
  ).map( _ % scalaTestVersion % "it,test")

  private val tapir = List(
    "com.softwaremill.sttp.tapir" %% "tapir-core",
    "com.softwaremill.sttp.tapir" %% "tapir-http4s-server",
    "com.softwaremill.sttp.tapir" %% "tapir-json-circe",
    "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle"
  ).map(_ % tapirVersion)

  val list: List[ModuleID] =
    cats ++
      catsEffect ++
      circe ++
      ciris ++
      doobie ++
      enumeratum ++
      http4s ++
      http4sJettyServer ++
      sangria ++
      scalaTest ++
      tapir

}

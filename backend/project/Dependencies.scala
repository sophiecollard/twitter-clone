import sbt._

object Dependencies {

  private val catsVersion = "2.7.0"
  private val catsEffectVersion = "3.3.12"
  private val circeVersion = "0.14.2"
  private val cirisVersion = "2.3.3"
  private val doobieVersion = "1.0.0-RC1"
  private val http4sVersion = "0.23.12"
  private val http4sJettyServerVersion = "0.23.10"
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
    "io.circe" %% "circe-generic"
  ).map(_ % circeVersion)

  private val ciris = List(
    "is.cir" %% "ciris"
  ).map(_ % cirisVersion)

  private val doobie = List(
    "org.tpolecat" %% "doobie-core",
    "org.tpolecat" %% "doobie-postgres"
  ).map(_ % doobieVersion)

  private val http4s = List(
    "org.http4s" %% "http4s-circe",
    "org.http4s" %% "http4s-core",
    "org.http4s" %% "http4s-dsl",
    "org.http4s" %% "http4s-server"
  ).map(_ % http4sVersion)

  private val http4sJettyServer = List(
    "org.http4s" % "http4s-jetty-server_2.13"
  ).map(_ % http4sJettyServerVersion)

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
      http4s ++
      http4sJettyServer ++
      scalaTest ++
      tapir

}

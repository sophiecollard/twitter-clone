import sbt._

object Dependencies {

  private val catsVersion = "2.7.0"
  private val catsEffectVersion = "3.3.12"
  private val circeVersion = "0.14.2"
  private val doobieVersion = "1.0.0-RC1"
  private val http4sVersion = "0.23.12"
  private val http4sJettyServerVersion = "0.23.10"
  private val scalaTestVersion = "3.2.11"

  private val cats = Seq(
    "org.typelevel" %% "cats-core"
  ).map(_ % catsVersion)

  private val catsEffect = Seq(
    "org.typelevel" %% "cats-effect"
  ).map(_ % catsEffectVersion)

  private val circe = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic"
  ).map(_ % circeVersion)

  private val doobie = Seq(
    "org.tpolecat" %% "doobie-core"
  ).map(_ % doobieVersion)

  private val http4s = Seq(
    "org.http4s" %% "http4s-circe",
    "org.http4s" %% "http4s-core",
    "org.http4s" %% "http4s-dsl",
    "org.http4s" %% "http4s-server"
  ).map(_ % http4sVersion)

  private val http4sJettyServer = Seq(
    "org.http4s" % "http4s-jetty-server_2.13"
  ).map(_ % http4sJettyServerVersion)

  private val scalaTest = Seq(
    "org.scalatest" %% "scalatest"
  ).map( _ % scalaTestVersion % Test)

  val asSeq: Seq[ModuleID] =
    cats ++
      catsEffect ++
      circe ++
      doobie ++
      http4s ++
      http4sJettyServer ++
      scalaTest

}

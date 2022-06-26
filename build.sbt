import Dependencies._

ThisBuild / scalaVersion     := "2.13.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.github.sophiecollard"
ThisBuild / organizationName := "SophieCollard"

lazy val root = (project in file("."))
  .settings(
    name := "twitter-clone",
    libraryDependencies ++= asSeq,
    resolvers += "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases/"
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.

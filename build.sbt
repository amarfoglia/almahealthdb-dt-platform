import Dependencies.*

val scala3Version = "3.2.0"

ThisBuild / name := "almahealthdb-dt-platform"
ThisBuild / scalaVersion := scala3Version
ThisBuild / version := "0.1.0-SNAPSHOT"

lazy val root = project
  .in(file("."))
  .aggregate(
    core,
  )

lazy val core = project
  .in(file("core"))
  .settings(
    libraryDependencies ++= Seq(
      dev.zio.zio,
      dev.zio.`zio-streams`,
      dev.zio.`zio-test`,
      dev.zio.`zio-test-junit`,
    )
  )
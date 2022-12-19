import Dependencies.*

val scala3Version = "3.2.0"

ThisBuild / name := "almahealthdb-dt-platform"
ThisBuild / scalaVersion := scala3Version
ThisBuild / version := "0.1.0-SNAPSHOT"

run / fork := false
Global / cancelable := false

lazy val root = project
  .in(file("."))
  .aggregate(
    domain,
    core,
    delivery,
    main
  )

lazy val domain = project
  .in(file("domain"))
  .settings(
    libraryDependencies ++= Seq(
      `ca.uhn.hapi.fhir`.`hapi-fhir-structures-r4`,
    )
  )

lazy val core = project
  .in(file("core"))
  .dependsOn(domain)
  .settings(
    libraryDependencies ++= Seq(
      dev.zio.zio,
      dev.zio.`zio-kafka`,
      dev.zio.`zio-streams`,
    )
  )

lazy val `event-port-kafka`= project
  .in(file("event-port-kafka"))
  .dependsOn(core)
  .settings(
    libraryDependencies ++= Seq(
      dev.zio.zio,
      dev.zio.`zio-kafka`,
      dev.zio.`zio-streams`,
    )
  )

lazy val `repository-in-memory` = project
  .in(file("repository-in-memory"))
  .dependsOn(core)

lazy val delivery = project
  .in(file("delivery"))
  .dependsOn(
    core,
  )
  .settings(
    libraryDependencies ++= Seq(
      dev.zio.zio,
      dev.zio.`zio-http`,
      dev.zio.`zio-json`,
    )
  )

lazy val main = project
  .in(file("main"))
  .dependsOn(
    core,
    delivery,
    `repository-in-memory`,
    `event-port-kafka`,
  )

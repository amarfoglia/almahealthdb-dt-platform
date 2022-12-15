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
    core,
    delivery,
    main
  )

lazy val main = project
  .in(file("main"))
  .dependsOn(
    core,
    delivery
  )

lazy val delivery = project
  .in(file("delivery"))
  .dependsOn(
    core
  )
  .settings(
    libraryDependencies ++= Seq(
      dev.zio.zio,
      dev.zio.`zio-http`,
      dev.zio.`zio-json`,
    )
  )

lazy val core = project
  .in(file("core"))
  .settings(
    libraryDependencies ++= Seq(
      dev.zio.zio,
      dev.zio.`zio-http`,
      dev.zio.`zio-streams`,
      `ca.uhn.hapi.fhir`.`hapi-fhir-base`,
      // `ca.uhn.hapi.fhir`.`hapi-fhir-structures-dstu2`,
      `ca.uhn.hapi.fhir`.`hapi-fhir-structures-r4`,
    )
  )
import Dependencies.*

ThisBuild / name := "almahealthdb-dt-platform"
ThisBuild / scalaVersion := "3.2.0"
ThisBuild / version := "0.1.0-SNAPSHOT"

run / fork := false
Global / cancelable := false

lazy val root = project.in(file("."))
  .aggregate(
    `pss-patient`,
    `fall-detection`
  )

lazy val `pss-patient` = project.in(file("pss-patient"))
  .aggregate(`pss-patient-domain`)

lazy val `pss-patient-domain` = project.in(file("pss-patient/domain"))
  .settings(
    libraryDependencies ++= Seq(
      `ca.uhn.hapi.fhir`.`hapi-fhir-structures-r4`,
    )
  )

lazy val `pss-patient-core` = project.in(file("pss-patient/core"))
  .dependsOn(`pss-patient-domain`)
  .settings(
    libraryDependencies ++= Seq(
      dev.zio.zio,
      dev.zio.`zio-kafka`,
      dev.zio.`zio-streams`,
    )
  )

lazy val `pss-patient-event-port-kafka`= project.in(file("pss-patient/event-port-kafka"))
  .dependsOn(`pss-patient-core`)
  .settings(
    libraryDependencies ++= Seq(
      dev.zio.zio,
      dev.zio.`zio-kafka`,
      dev.zio.`zio-streams`,
    )
  )

lazy val `pss-patient-repository-in-memory` = project.in(file("pss-patient/repository-in-memory"))
  .dependsOn(`pss-patient-core`)

lazy val `pss-patient-delivery` = project.in(file("pss-patient/delivery"))
  .dependsOn(
    `pss-patient-core`,
  )
  .settings(
    libraryDependencies ++= Seq(
      dev.zio.zio,
      dev.zio.`zio-http`,
      dev.zio.`zio-json`,
    )
  )

lazy val `fall-detection` = project.in(file("fall-detection"))
  .aggregate(
    `fall-detection-domain`,
    `fall-detection-core`,
    `fall-detection-repository-in-memory`,
    `fall-detection-delivery`,
  )

lazy val `fall-detection-domain` = project.in(file("fall-detection/domain"))

lazy val `fall-detection-core` = project.in(file("fall-detection/core"))
  .dependsOn(`fall-detection-domain`)

lazy val `fall-detection-repository-in-memory` = project.in(file("fall-detection/repository-in-memory"))
  .dependsOn(`fall-detection-core`)

lazy val `fall-detection-delivery` = project.in(file("fall-detection/delivery"))
  .dependsOn(`fall-detection-core`)

lazy val main = project
  .in(file("main"))
  .dependsOn(
    `pss-patient-event-port-kafka`,
    `pss-patient-delivery`
  )

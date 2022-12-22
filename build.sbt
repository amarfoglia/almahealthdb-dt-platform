import Dependencies.*

ThisBuild / name := "almahealthdb-dt-platform"
ThisBuild / scalaVersion := "3.2.0"
ThisBuild / version := "0.1.0-SNAPSHOT"

run / fork := false
Global / cancelable := false

lazy val root = project.in(file("."))
  .aggregate(
    `pss-patient`,
    `fall-detection`,
    common
  )

lazy val common = project.in(file("common"))
  .aggregate(
    `common-domain`,
    `common-fhir`,
    `common-event-input-port`,
    `common-event-input-port-kafka`,
  )

lazy val `common-domain` = project.in(file("common/domain"))


lazy val `common-fhir` = project.in(file("common/fhir"))
  .dependsOn(
    `common-domain`,
    `common-event-input-port`,
  )
  .settings(
    libraryDependencies ++= Seq(
      dev.zio.zio,
      dev.zio.`zio-streams`,
      `ca.uhn.hapi.fhir`.`hapi-fhir-structures-r4`,
    )
  )

lazy val `common-event-input-port` = project.in(file("common/event-input-port"))
  .settings(
    libraryDependencies ++= Seq(
      dev.zio.zio,
      dev.zio.`zio-streams`,
    )
  )

lazy val `common-event-input-port-kafka`= project.in(file("common/event-input-port-kafka"))
  .dependsOn(`common-event-input-port`)
  .settings(
    libraryDependencies ++= Seq(
      dev.zio.zio,
      dev.zio.`zio-kafka`,
      dev.zio.`zio-streams`,
    )
  )

lazy val `pss-patient` = project.in(file("pss-patient"))
  .aggregate(
    `pss-patient-domain`,
    `pss-patient-core`,
    `pss-patient-delivery-http`,
    `pss-patient-repository-in-memory`,
  )

lazy val `pss-patient-domain` = project.in(file("pss-patient/domain"))
  .dependsOn(`common-domain`)
  .settings(
    libraryDependencies ++= Seq(
      `ca.uhn.hapi.fhir`.`hapi-fhir-structures-r4`,
    )
  )

lazy val `pss-patient-core` = project.in(file("pss-patient/core"))
  .dependsOn(
    `pss-patient-domain`,
    `common-fhir`,
  )
  .settings(
    libraryDependencies ++= Seq(
      dev.zio.zio,
      dev.zio.`zio-streams`,
    )
  )

lazy val `pss-patient-repository-in-memory` = project.in(file("pss-patient/repository-in-memory"))
  .dependsOn(`pss-patient-core`)

lazy val `pss-patient-delivery-http` = project.in(file("pss-patient/delivery-http"))
  .dependsOn(
    `pss-patient-core`,
    `common-fhir`,
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
    `fall-detection-delivery-http`,
  )

lazy val `fall-detection-domain` = project.in(file("fall-detection/domain"))
  .dependsOn(`common-domain`)
  .settings(
    libraryDependencies ++= Seq(
      `ca.uhn.hapi.fhir`.`hapi-fhir-structures-r4`,
    )
  )

lazy val `fall-detection-core` = project.in(file("fall-detection/core"))
  .dependsOn(
    `fall-detection-domain`,
    `common-fhir`,
    `common-event-input-port`,
  )
  .settings(
    libraryDependencies ++= Seq(
      dev.zio.zio,
      dev.zio.`zio-streams`,
    )
  )

lazy val `fall-detection-repository-in-memory` = project.in(file("fall-detection/repository-in-memory"))
  .dependsOn(`fall-detection-core`)

lazy val `fall-detection-delivery-http` = project.in(file("fall-detection/delivery-http"))
  .dependsOn(`fall-detection-core`)

lazy val main = project.in(file("main"))
  .dependsOn(
    `common-fhir`,
    `common-event-input-port-kafka`,
    `fall-detection-core`,
  )
  .settings(
    libraryDependencies ++= Seq(
      dev.zio.zio,
      dev.zio.`zio-streams`,
      dev.zio.`zio-kafka`,
    )
  )

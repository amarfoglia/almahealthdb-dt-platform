import Dependencies.*

ThisBuild / scalaVersion := "3.2.0"
ThisBuild / version      := "0.1.0-SNAPSHOT"

lazy val commonConfiguration = Seq(
  testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
  Test / testOptions += Tests.Argument("-ignore-tags", "stardog"),
  libraryDependencies ++= Seq(
    dev.zio.zio,
    dev.zio.`zio-streams`,
    dev.zio.`zio-mock`          % Test,
    dev.zio.`zio-test`          % Test,
    dev.zio.`zio-test-sbt`      % Test,
    dev.zio.`zio-test-magnolia` % Test
  )
)

lazy val root = project
  .in(file("."))
  .aggregate(
    `pss-patient`,
    `fall-detection`,
    common,
    examples
  )

lazy val common = project
  .in(file("common"))
  .aggregate(
    `common-domain`,
    `common-fhir`,
    `common-stardog`,
    `common-event-input-port`,
    `common-event-input-port-kafka`
  )

lazy val `common-domain` = project
  .in(file("common/domain"))
  .settings(commonConfiguration)

lazy val `common-fhir` = project
  .in(file("common/fhir"))
  .dependsOn(
    `common-domain`,
    `common-event-input-port`
  )
  .settings(commonConfiguration)
  .settings(
    libraryDependencies ++= Seq(
      `ca.uhn.hapi.fhir`.`hapi-fhir-structures-r4`
    )
  )

lazy val `common-stardog` = project
  .in(file("common/stardog"))
  .settings(commonConfiguration)
  .settings(
    resolvers += "Stardog Maven" at "https://maven.stardog.com/",
    libraryDependencies ++= Seq(
      `com.complexible.stardog`.`client-http`
    )
  )

lazy val `common-event-input-port` = project
  .in(file("common/event-input-port"))
  .settings(commonConfiguration)

lazy val `common-event-input-port-kafka` = project
  .in(file("common/event-input-port-kafka"))
  .dependsOn(`common-event-input-port`)
  .settings(commonConfiguration)
  .settings(
    libraryDependencies ++= Seq(
      dev.zio.`zio-kafka`
    )
  )

lazy val `pss-patient` = project
  .in(file("pss-patient"))
  .aggregate(
    `pss-patient-domain`,
    `pss-patient-core`,
    `pss-patient-delivery-http`,
    `pss-patient-repository-in-memory`,
    `pss-patient-repository-stardog`
  )

lazy val `pss-patient-domain` = project
  .in(file("pss-patient/domain"))
  .dependsOn(`common-domain`)
  .settings(commonConfiguration)
  .settings(
    libraryDependencies ++= Seq(
      `ca.uhn.hapi.fhir`.`hapi-fhir-structures-r4`
    )
  )

lazy val `pss-patient-core` = project
  .in(file("pss-patient/core"))
  .dependsOn(
    `pss-patient-domain`,
    `common-fhir`
  )
  .settings(commonConfiguration)

lazy val `pss-patient-repository-in-memory` = project
  .in(file("pss-patient/repository-in-memory"))
  .dependsOn(`pss-patient-core`)
  .settings(commonConfiguration)

lazy val `pss-patient-repository-stardog` = project
  .in(file("pss-patient/repository-stardog"))
  .dependsOn(`common-fhir`, `common-stardog`, `pss-patient-core`)
  .settings(commonConfiguration)
  .settings(
    libraryDependencies ++= Seq(
      `com.complexible.stardog`.`client-http`,
      `org.apache.jena`.jena
    )
  )

lazy val `pss-patient-delivery-http` = project
  .in(file("pss-patient/delivery-http"))
  .dependsOn(
    `pss-patient-core`,
    `common-fhir`
  )
  .settings(commonConfiguration)
  .settings(
    libraryDependencies ++= Seq(
      dev.zio.`zio-http`,
      dev.zio.`zio-json`
    )
  )

lazy val `fall-detection` = project
  .in(file("fall-detection"))
  .aggregate(
    `fall-detection-domain`,
    `fall-detection-core`,
    `fall-detection-repository-in-memory`,
    `fall-detection-delivery-http`
  )

lazy val `fall-detection-domain` = project
  .in(file("fall-detection/domain"))
  .dependsOn(`common-domain`)
  .settings(commonConfiguration)
  .settings(
    libraryDependencies ++= Seq(
      `ca.uhn.hapi.fhir`.`hapi-fhir-structures-r4`
    )
  )

lazy val `fall-detection-core` = project
  .in(file("fall-detection/core"))
  .dependsOn(
    `fall-detection-domain`,
    `common-fhir`,
    `common-event-input-port`
  )
  .settings(commonConfiguration)

lazy val `fall-detection-repository-in-memory` = project
  .in(file("fall-detection/repository-in-memory"))
  .dependsOn(`fall-detection-core`)
  .settings(commonConfiguration)

lazy val `fall-detection-delivery-http` = project
  .in(file("fall-detection/delivery-http"))
  .dependsOn(`fall-detection-core`)
  .settings(commonConfiguration)

lazy val examples = project
  .in(file("examples"))
  .aggregate(`examples-pss-patient-ms`)

lazy val `examples-pss-patient-ms` = project
  .in(file("examples/pss-patient-ms"))
  .dependsOn(
    `pss-patient-core`,
    `pss-patient-delivery-http`,
    `pss-patient-repository-stardog`
  )
  .settings(commonConfiguration)
  .settings(
    libraryDependencies ++= Seq(
      `org.apache.jena`.jena
    )
  )

lazy val main = project
  .in(file("main"))
  .dependsOn(
    `common-fhir`,
    `common-event-input-port-kafka`,
    `fall-detection-core`
  )
  .settings(commonConfiguration)
  .settings(
    libraryDependencies ++= Seq(
      dev.zio.`zio-kafka`
    )
  )

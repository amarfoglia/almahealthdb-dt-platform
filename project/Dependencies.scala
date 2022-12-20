import sbt.*

object Dependencies {
  object dev {
    object zio {
      private val zioVersion = "2.0.2"

      val zio = "dev.zio" %% "zio" % zioVersion
      val `zio-http` = "dev.zio" %% "zio-http" % "0.0.3"
      val `zio-json` = "dev.zio" %% "zio-json" % "0.3.0"
      val `zio-kafka` = "dev.zio" %% "zio-kafka" % "2.0.1"
      val `zio-streams` = "dev.zio" %% "zio-streams" % zioVersion
      val `zio-test` = "dev.zio" %% "zio-test" % zioVersion
      val `zio-test-junit` = "dev.zio" %% "zio-test-junit" % zioVersion
    }
  }
  object `ca.uhn.hapi.fhir` {
    private val hapiFhirVersion = "6.2.2"

    val `hapi-fhir-base` = "ca.uhn.hapi.fhir" % "hapi-fhir-base" % hapiFhirVersion
    val `hapi-fhir-structures-dstu2` = "ca.uhn.hapi.fhir" % "hapi-fhir-structures-dstu2" % hapiFhirVersion
    val `hapi-fhir-structures-r4` = "ca.uhn.hapi.fhir" % "hapi-fhir-structures-r4" % hapiFhirVersion
  }
}

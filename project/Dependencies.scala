import sbt.*

object Dependencies {
  object `org.apache.jena` {
    private val jenaVersion = "3.17.0"
    val `jena-core`         = "org.apache.jena" % "jena-core" % jenaVersion
    val `jena-arq`          = "org.apache.jena" % "jena-arq"  % jenaVersion
  }
  object `com.complexible.stardog` {
    val `client-http` = "com.complexible.stardog" % "client-http" % "8.1.1" pomOnly ()
  }
  object dev {
    object zio {
      private val zioVersion = "2.0.5"

      val zio                 = "dev.zio" %% "zio"               % zioVersion
      val `zio-http`          = "dev.zio" %% "zio-http"          % "0.0.3"
      val `zio-json`          = "dev.zio" %% "zio-json"          % "0.3.0"
      val `zio-kafka`         = "dev.zio" %% "zio-kafka"         % "2.0.2"
      val `zio-streams`       = "dev.zio" %% "zio-streams"       % zioVersion
      val `zio-test`          = "dev.zio" %% "zio-test"          % zioVersion
      val `zio-test-magnolia` = "dev.zio" %% "zio-test-magnolia" % zioVersion
      val `zio-test-sbt`      = "dev.zio" %% "zio-test-sbt"      % zioVersion
      val `zio-test-junit`    = "dev.zio" %% "zio-test-junit"    % zioVersion
    }
  }
  object `ca.uhn.hapi.fhir` {
    private val hapiFhirVersion = "5.7.9"

    val `hapi-fhir-base` = "ca.uhn.hapi.fhir" % "hapi-fhir-base" % hapiFhirVersion
    val `hapi-fhir-structures-dstu2` =
      "ca.uhn.hapi.fhir" % "hapi-fhir-structures-dstu2" % hapiFhirVersion
    val `hapi-fhir-structures-r4` = "ca.uhn.hapi.fhir" % "hapi-fhir-structures-r4" % hapiFhirVersion
  }
}

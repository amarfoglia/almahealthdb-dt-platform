import sbt.*

object Dependencies {
    object dev {
        object zio {
            private val zioVersion = "2.0.2"

            val zio = "dev.zio" %% "zio" % zioVersion
            val `zio-json` = "dev.zio" %% "zio-json" % "0.3.0"
            val `zio-streams` = "dev.zio" %% "zio" % zioVersion
            val `zio-test` = "dev.zio" %% "zio-test" % zioVersion
            val `zio-test-junit` = "dev.zio" %% "zio-test-junit" % zioVersion
        }
    }
}
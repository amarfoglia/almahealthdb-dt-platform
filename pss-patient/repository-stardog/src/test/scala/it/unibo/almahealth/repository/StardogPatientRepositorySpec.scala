package it.unibo.almahealth.repository

import com.complexible.stardog.api.ConnectionConfiguration
import com.complexible.stardog.api.ConnectionPool
import com.complexible.stardog.api.ConnectionPoolConfig
import it.unibo.almahealth.domain.Identifier
import it.unibo.almahealth.stardog.ZConnectionPool
import zio.ZIO
import zio.test.*
import it.unibo.almahealth.context.ZFhirContext.apply
import ca.uhn.fhir.context.FhirContext
import it.unibo.almahealth.context.ZFhirContext

object StardogPatientRepositorySpec extends ZIOSpecDefault:
  def spec = suite("StardogPatientRepositorySpec")(
    test("findById should return a patient") {
      def connectionConfig = ConnectionConfiguration
        .to("testDB")
        .server("http://localhost:5820")
        .credentials("admin", "admin")
      def makeConnectionPool = ZIO
        .attempt {
          ConnectionPoolConfig
            .using(connectionConfig)
            .create()
        }
      def zFhirContext = ZFhirContext(FhirContext.forR4())

      def makeRepository = makeConnectionPool
        .map(ZConnectionPool(_))
        .map(StardogPatientRepository(_, zFhirContext))

      for
        repository <- makeRepository
        patient    <- repository.findById(Identifier("GTWGWY82B42G920M"))
        encoder    <- zFhirContext.newJsonEncoder
        serialized <- encoder.encodeResourceToString(patient)
        _          <- ZIO.debug(serialized)
      yield assertTrue(true)

    }
  )

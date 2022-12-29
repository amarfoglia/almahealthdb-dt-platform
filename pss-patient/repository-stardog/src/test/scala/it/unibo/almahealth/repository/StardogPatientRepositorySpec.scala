package it.unibo.almahealth.repository

import ca.uhn.fhir.context.FhirContext
import com.complexible.stardog.api.ConnectionConfiguration
import com.complexible.stardog.api.ConnectionPool
import com.complexible.stardog.api.ConnectionPoolConfig
import it.unibo.almahealth.context.ZFhirContext
import it.unibo.almahealth.context.ZFhirContext.apply
import it.unibo.almahealth.domain.Identifier
import it.unibo.almahealth.stardog.ZConnectionPool
import it.unibo.almahealth.stardog.ZTurtleWriter
import org.hl7.fhir.r4.model.Bundle
import zio.ZIO
import zio.test.TestAspect.ignore
import zio.test.TestAspect.tag
import zio.test.*

object StardogPatientRepositorySpec extends ZIOSpecDefault:

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
    .map(StardogPatientRepository(_, zFhirContext, ZTurtleWriter()))
  def spec = suite("StardogPatientRepositorySpec")(
    test("findById should return a patient") {
      for
        repository <- makeRepository
        patient    <- repository.findById(Identifier("GTWGWY82B42G920M"))
      yield assertTrue(patient != null)

    },
    test("getAllergyIntolerances should return a Bundle") {
      for
        repository <- makeRepository
        bundle     <- repository.getAllergyIntolerances(Identifier("GTWGWY82B42G920M"))
      yield assertTrue(bundle != null)
    },
    test("getFunctionalStatus should return a Bundle") {
      for
        repository <- makeRepository
        bundle     <- repository.getFunctionalStatus(Identifier("GTWGWY82B42G920M"))
      yield assertTrue(bundle != null)
    },
    test("getImmunizations should return a Bundle") {
      for
        repository <- makeRepository
        bundle     <- repository.getImmunizations(Identifier("GTWGWY82B42G920M"))
      yield assertTrue(bundle != null)
    },
    test("getMedicalEquipment should return a Bundle") {
      for
        repository <- makeRepository
        bundle     <- repository.getMedicalEquipment(Identifier("GTWGWY82B42G920M"))
      yield assertTrue(bundle != null)
    },
    test("getMedications should return a Bundle") {
      for
        repository <- makeRepository
        bundle     <- repository.getMedications(Identifier("GTWGWY82B42G920M"))
      yield assertTrue(bundle != null)
    },
    test("getProblems should return a Bundle") {
      for
        repository <- makeRepository
        bundle     <- repository.getProblems(Identifier("GTWGWY82B42G920M"))
      yield assertTrue(bundle != null)
    },
    test("getProcedures should return a Bundle") {
      for
        repository <- makeRepository
        bundle     <- repository.getProcedures(Identifier("GTWGWY82B42G920M"))
      yield assertTrue(bundle != null)
    },
    test("getSocialHistory should return a Bundle") {
      for
        repository <- makeRepository
        bundle     <- repository.getSocialHistory(Identifier("GTWGWY82B42G920M"))
      yield assertTrue(bundle != null)
    },
    test("getVitalSigns should return a Bundle") {
      for
        repository <- makeRepository
        bundle     <- repository.getVitalSigns(Identifier("GTWGWY82B42G920M"))
      yield assertTrue(bundle != null)
    },
    test("uploadDocument should return success") {
      for
        repository <- makeRepository
        bundle     <- repository.uploadDocument(new Bundle())
      yield assertTrue(true)
    }
  ) @@ tag("stardog")

package it.unibo.almahealth

import ca.uhn.fhir.parser.DataFormatException
import com.complexible.stardog.api.ConnectionConfiguration
import com.complexible.stardog.api.ConnectionPoolConfig
import com.complexible.stardog.api.admin.AdminConnection
import com.complexible.stardog.api.admin.AdminConnectionConfiguration
import it.unibo.almahealth.context.ZFhirContext
import it.unibo.almahealth.delivery.PatientApp
import it.unibo.almahealth.presenter.ResourcePresenter
import it.unibo.almahealth.repository.StardogPatientRepository
import it.unibo.almahealth.service.PatientService
import it.unibo.almahealth.stardog.ZConnectionPool
import it.unibo.almahealth.stardog.ZWriter
import org.hl7.fhir.r4.model.AllergyIntolerance
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Condition
import org.hl7.fhir.r4.model.Device
import org.hl7.fhir.r4.model.Immunization
import org.hl7.fhir.r4.model.MedicationStatement
import org.hl7.fhir.r4.model.MedicationStatement.MedicationStatementStatus
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Procedure
import org.hl7.fhir.r4.model.Procedure.ProcedureStatus
import zio.Exit
import zio.Fiber
import zio.Scope
import zio.Task
import zio.UIO
import zio.URIO
import zio.ZIO
import zio.ZLayer
import zio.durationInt
import zio.http.Body
import zio.http.Client
import zio.http.Http
import zio.http.Response
import zio.http.Server
import zio.http.ServerConfig
import zio.http.ZClient
import zio.http.model.Method
import zio.http.model.Status
import zio.test.TestAspect
import zio.test.TestClock
import zio.test.TestResult
import zio.test.ZIOSpecDefault
import zio.test.assertTrue

import java.io.File
import scala.annotation.nowarn
import scala.io.Source

import util.chaining.*

object IntegrationTest extends ZIOSpecDefault {
  def connectionConfig = ConnectionConfiguration
    .to("testDB")
    .server("http://localhost:5820")
    .credentials("admin", "admin")

  def makeConnectionPool = ZIO.attempt {
    ConnectionPoolConfig
      .using(connectionConfig)
      .create()
  }

  def httpServer = ZLayer.scoped(
    ZIO.acquireRelease(Server.serve(PatientApp.http).fork.interruptible)(_.interrupt)
  )

  def devConfig = ServerConfig.live(ServerConfig.default.port(8080))

  def patientService: ZLayer[ZFhirContext, Throwable, PatientService] =
    (ZConnectionPool.live(connectionConfig) ++ ZWriter.live.turtle) >>>
      StardogPatientRepository.live >>>
      PatientService.live

  def patientApp: ZLayer[ZFhirContext, Throwable, PatientApp] =
    (patientService ++ ResourcePresenter.json) >>>
      PatientApp.live

  def testDBLayer =
    def makeAdminConnection = ZIO.attemptBlocking {
      AdminConnectionConfiguration
        .toServer("http://localhost:5820")
        .credentials("admin", "admin")
        .connect()
    }
    def acquire: ZIO[Any, Throwable, Unit] =
      for
        conn <- makeAdminConnection
        _ <-
          if conn.list().contains("testDB") then ZIO.attemptBlocking(conn.drop("testDB"))
          else ZIO.unit
        _ <- ZIO.attemptBlocking(conn.disk("testDB").create()): @nowarn("msg=deprecated")
      yield ()

    def release: ZIO[Any, Nothing, Unit] = (
      for
        conn <- makeAdminConnection
        _ <-
          if conn.list().contains("testDB") then ZIO.attemptBlocking(conn.drop("testDB"))
          else ZIO.unit
      yield ()
    ).orDie

    ZLayer.scoped(ZIO.acquireRelease(acquire)(_ => release))

  def deps =
    ZFhirContext.live.forR4 >+>
      ((devConfig >>> Server.live ++ patientApp) >>> httpServer) ++ Client.default ++ testDBLayer

  def makeUrl(path: String) = s"http://localhost:8080/${path}"

  val patientId = "GTWGWY82B42G920M"

  def testResource(
      resourceName: String
  )(f: Bundle => TestResult): ZIO[Client & ZFhirContext, Throwable, TestResult] =
    for
      res <- Client.request(url = makeUrl(s"${patientId}/${resourceName}"))
      _ <-
        if res.status != Status.Ok
        then ZIO.fail(new RuntimeException(s"Status code: ${res.status.code}"))
        else ZIO.unit
      body   <- res.body.asString
      parser <- ZFhirContext.newJsonParser
      bundle <- parser.parseString(classOf[Bundle], body)
    yield {
      assertTrue(res.status == Status.Ok) &&
      f(bundle)
    }

  def spec = (suite("PssPatientIntegrationTest")(
    test("POST /uploadDocument") {
      for
        pssBody <- ZIO.succeedBlocking(Source.fromResource("pss.json")).map(_.getLines().mkString)
        req <- Client.request(
          url = makeUrl("uploadDocument"),
          method = Method.POST,
          content = Body.fromString(pssBody)
        )
      yield assertTrue(req.status == Status.Ok)
    },
    test("GET /:patientId") {
      for
        req     <- Client.request(url = makeUrl(s"/${patientId}"))
        body    <- req.body.asString
        parser  <- ZFhirContext.newJsonParser
        patient <- parser.parseString(classOf[Patient], body)
      yield {
        assertTrue(req.status == Status.Ok) &&
        assertTrue(patient.getName().get(0).getGiven().get(0).toString == "Guido")
      }
    },
    test("GET /:patientId/allergyIntolerances") {
      testResource("allergyIntolerances") { bundle =>
        assertTrue(bundle.getEntry().size() == 1) &&
        assertTrue(
          bundle
            .getEntry()
            .get(0)
            .getResource()
            .asInstanceOf[AllergyIntolerance]
            .getReaction()
            .get(0)
            .getSubstance()
            .getCoding()
            .get(0)
            .getCode() == "260152009"
        )
      }
    },
    test("GET /:patientId/medications") {
      testResource("medications") { bundle =>
        assertTrue(
          bundle
            .getEntry()
            .get(0)
            .getResource()
            .asInstanceOf[MedicationStatement]
            .getStatus() == MedicationStatementStatus.COMPLETED
        )
      }
    },
    test("GET /:patientId/problems") {
      testResource("problems") { bundle =>
        assertTrue(
          bundle
            .getEntry()
            .get(0)
            .getResource()
            .asInstanceOf[Condition]
            .getClinicalStatus()
            .getCoding()
            .get(0)
            .getCode() == "LA18632-2"
        )
      }
    },
    test("GET /:patientId/medicalEquipment") {
      testResource("medicalEquipment") { bundle =>
        assertTrue(
          bundle
            .getEntry()
            .get(0)
            .getResource()
            .asInstanceOf[Device]
            .getType()
            .getCoding()
            .get(0)
            .getCode() == "J010103"
        )
      }
    },
    test("GET /:patientId/procedures") {
      testResource("procedures") { bundle =>
        assertTrue(
          bundle
            .getEntry()
            .get(0)
            .getResource()
            .asInstanceOf[Procedure]
            .getStatus() == ProcedureStatus.INPROGRESS
        )
      }
    },
    test("GET /:patientId/functionalStatus") {
      testResource("functionalStatus") { bundle =>
        assertTrue(
          bundle
            .getEntry()
            .get(0)
            .getResource()
            .asInstanceOf[Observation]
            .getCode()
            .getCoding()
            .get(0)
            .getCode() == "75246-9"
        )
      }
    },
    test("GET /:patientId/immunizations") {
      testResource("immunizations") { bundle =>
        assertTrue(
          bundle
            .getEntry()
            .get(0)
            .getResource()
            .asInstanceOf[Immunization]
            .getVaccineCode()
            .getCoding()
            .get(0)
            .getCode() == "035911015"
        )
      }
    },
    test("GET /:patientId/socialHistory") {
      testResource("socialHistory") { bundle =>
        assertTrue(
          bundle
            .getEntry()
            .get(0)
            .getResource()
            .asInstanceOf[Observation]
            .getCode()
            .getCoding()
            .get(0)
            .getCode() == "74013-4"
        )
      }
    },
    test("GET /:patientId/vitalSigns") {
      testResource("vitalSigns") { bundle =>
        assertTrue(
          bundle
            .getEntry()
            .get(0)
            .getResource()
            .asInstanceOf[Observation]
            .getCode()
            .getCoding()
            .get(0)
            .getCode() == "8480-6"
        )
      }
    },
    test("Upload a new PSS version and check updates") {
      for
        pssBody <- ZIO.succeedBlocking(Source.fromResource("pss2.json")).map(_.getLines().mkString)
        uploadRes <- Client.request(
          url = makeUrl("uploadDocument"),
          method = Method.POST,
          content = Body.fromString(pssBody)
        )
        allergyRes <- Client.request(url = makeUrl(s"/${patientId}/allergyIntolerances"))
        body       <- allergyRes.body.asString
        parser     <- ZFhirContext.newJsonParser
        bundle     <- parser.parseString(classOf[Bundle], body)
      yield {
        assertTrue(uploadRes.status == Status.Ok) &&
        assertTrue(bundle.getEntry().size() == 2)
      }
    }
  ) @@ TestAspect.sequential @@ TestAspect.tag("stardog") @@ TestAspect.ignore)
    .provideLayerShared(deps)
}

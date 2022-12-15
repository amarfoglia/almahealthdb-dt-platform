package it.unibo.almahealth

import zio.Scope
import zio.ZIO
import zio.ZIOAppArgs
import zio.ZIOAppDefault
import zio.http.*
import zio.http.model.Method

import it.unibo.almahealth.delivery.patientHttp
import it.unibo.almahealth.context.ZFhirContext
import it.unibo.almahealth.usecases.PatientUseCases
import it.unibo.almahealth.repository.InMemoryPatientRepository
import it.unibo.almahealth.domain.Identifier
import org.hl7.fhir.r4.model.Patient
import zio.ZLayer
import it.unibo.almahealth.repository.PatientRepository

object DeliveryMain extends ZIOAppDefault:
  val devConfig = ServerConfig.live(ServerConfig.default.port(8080))

  val app = patientHttp

  val program = Server.install(app) *> zio.Console.printLine("Server started on port 8080") *> ZIO.never

  val patients = Map(
    Identifier("0000") -> Patient(),
    Identifier("0001") -> Patient(),
  )

  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] = 
    program
      .provide(
        devConfig,
        Server.live,
        ZFhirContext.live.forR4,
        PatientRepository.inMemory(patients)
      )


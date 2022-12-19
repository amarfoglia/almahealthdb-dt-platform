package it.unibo.almahealth

import zio._
import zio.Scope
import zio.ZIO
import zio.ZIOAppArgs
import zio.ZIOAppDefault
import zio.ZLayer
import zio.http.*
import zio.http.model.Method
import zio.stream.ZStream

import it.unibo.almahealth.context.ZFhirContext
import it.unibo.almahealth.context.ZEncoder
import it.unibo.almahealth.repository.InMemoryPatientRepository
import it.unibo.almahealth.domain.Identifier
import org.hl7.fhir.r4.model.Patient
import it.unibo.almahealth.repository.PatientRepository
import it.unibo.almahealth.delivery.PatientApp
import it.unibo.almahealth.usecases.PatientService
import it.unibo.almahealth.usecases.FallDetectionService
import it.unibo.almahealth.presenter.PatientPresenter
import it.unibo.almahealth.events.EventInputPort
import it.unibo.almahealth.events.KafkaInputPort
import zio.kafka.consumer.ConsumerSettings

object Main extends ZIOAppDefault:
  val devConfig = ServerConfig.live(ServerConfig.default.port(8080))

  val app = PatientApp.http

  // val program = Server.install(app) *> zio.Console.printLine("Server started on port 8080") *> ZIO.never

  val program = for
    _ <- ZIO.debug("Start program")
    _ <- FallDetectionService.detectFalls
    .take(2)
    .flatMap(o => ZStream.fromZIO(ZIO.debug(o.getResourceType)))
    .runDrain
  yield ()

  // val patients = Map(
  //   Identifier("0000") -> Patient(),
  //   Identifier("0001") -> Patient(),
  // )


  val settings = ConsumerSettings(List("localhost:29092"))
    .withGroupId("group")
    .withClientId("client")
    .withCloseTimeout(30.seconds)

  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] = 
    program
      .onError(ZIO.debug(_))
      .provide(
        FallDetectionService.live,
        KafkaInputPort.live,
        ZLayer.succeed(settings),

        // devConfig,
        // Server.live,
        // PatientApp.live,
        // PatientService.live,
        // PatientPresenter.json,
        ZFhirContext.live.forR4,
        // InMemoryPatientRepository.live(patients)
      )


package it.unibo.almahealth

import zio.*
import zio.Scope
import zio.ZIO
import zio.ZIOAppArgs
import zio.ZIOAppDefault
import zio.ZLayer
import zio.stream.ZStream

import it.unibo.almahealth.usecases.FallDetectionService
import it.unibo.almahealth.events.KafkaInputPort
import zio.kafka.consumer.ConsumerSettings
import it.unibo.almahealth.context.ZFhirContext
import org.hl7.fhir.r4.model.Observation
import it.unibo.almahealth.events.EventInputPort

object Main extends ZIOAppDefault:
  // val devConfig = ServerConfig.live(ServerConfig.default.port(8080))

  // val app = PatientApp.http

  // val program = Server.install(app) *> zio.Console.printLine("Server started on port 8080") *> ZIO.never

  val program = for
    _ <- ZIO.debug("Start program")
    _ <- ZIO.scoped {
      FallDetectionService.fallStream
        .take(2)
        .flatMap(o => ZStream.fromZIO(ZIO.debug(o.getResourceType)))
        .runDrain
    }
  yield ()

  // val patients = Map(
  //   Identifier("0000") -> Patient(),
  //   Identifier("0001") -> Patient(),
  // )


  val settings = ConsumerSettings(List("localhost:29092"))
    .withGroupId("group")
    .withClientId("client")
    .withCloseTimeout(30.seconds)


  // def observations: ZLayer[ZFhirContext & EventInputPort[String], Nothing, EventInputPort[Observation]] =
  //   ZLayer {
  //     for
  //       parser <- ZFhirContext.newJsonParser
  //       stringInputPort <- ZIO.serviceWith[EventInputPort[String]](_.mapZIO(parser.parseString(classOf[Observation], _)))
  //     yield stringInputPort
  //   }

  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] = 
    program
      .onError(ZIO.debug(_))
      .provide(
        FallDetectionService.live,
        ZLayer.succeed(settings),
        ZFhirContext.live.forR4,
        KafkaInputPort.live >>> KafkaInputPort.deserialize { in =>
          ZFhirContext.newJsonParser
            .flatMap(_.parseString(classOf[Observation], in))
        },


        // devConfig,
        // Server.live,
        // PatientApp.live,
        // PatientService.live,
        // PatientPresenter.json,
        // ZFhirContext.live.forR4,
        // InMemoryPatientRepository.live(patients)
      )


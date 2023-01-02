package it.unibo.almahealth

import ca.uhn.fhir.parser.DataFormatException
import com.complexible.stardog.api.ConnectionConfiguration
import it.unibo.almahealth.context.ZFhirContext
import it.unibo.almahealth.delivery.PatientApp
import it.unibo.almahealth.presenter.ResourcePresenter
import it.unibo.almahealth.repository.StardogPatientRepository
import it.unibo.almahealth.service.PatientService
import it.unibo.almahealth.stardog.ZConnectionPool
import it.unibo.almahealth.stardog.ZTurtleWriter
import zio.Console
import zio.Scope
import zio.ZIO
import zio.ZIOAppArgs
import zio.ZIOAppDefault
import zio.ZLayer
import zio.http.Http
import zio.http.Response
import zio.http.Response.apply
import zio.http.Server
import zio.http.ServerConfig
import zio.http.model.Status

object Main extends ZIOAppDefault:
  val devConfig = ServerConfig.live(ServerConfig.default.port(8080))

  val connectionConfig = ConnectionConfiguration
    .to("testDB")
    .server("http://localhost:5820")
    .credentials("admin", "admin")

  val app = PatientApp.http
    .catchAll {
      case e: NoSuchElementException =>
        Http.fromZIO(ZIO.debug(e.getMessage)) *> Http.succeed(Response.status(Status.NotFound))
      case e: DataFormatException =>
        Http.fromZIO(ZIO.debug(e.getMessage)) *> Http.succeed(Response.status(Status.BadRequest))
    }

  val program = for
    port <- Server.install(app, Some(e => ZIO.debug(e)))
    _    <- Console.printLine(s"Server started on port: ${port}").orDie
    _    <- ZIO.never
  yield ()

  override def run = program
    .provide(
      devConfig,
      ZLayer.succeed(ZTurtleWriter()),
      ZConnectionPool.live(connectionConfig),
      StardogPatientRepository.live,
      ZFhirContext.live.forR4,
      ResourcePresenter.json,
      PatientService.live,
      PatientApp.live,
      Server.live
    )

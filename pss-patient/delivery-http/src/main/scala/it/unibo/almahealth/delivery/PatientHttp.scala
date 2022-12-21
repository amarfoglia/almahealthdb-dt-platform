package it.unibo.almahealth.delivery

import it.unibo.almahealth.domain.syntax.pattern.*
import zio.http.*
import zio.http.model.Method
import zio.ZIO
import ca.uhn.fhir.context.FhirContext
import it.unibo.almahealth.repository.PatientRepository
import it.unibo.almahealth.usecases.PatientService
import it.unibo.almahealth.presenter.PatientPresenter
import zio.ZLayer

class PatientApp(
  patientService: PatientService,
  patientPresenter: PatientPresenter
):
  def http: HttpApp[Any, NoSuchElementException] =
    Http.collectZIO[Request] {
      case Method.GET -> !! / Identifier(identifier) =>
        for
          patient <- patientService.findById(identifier)
          encoded <- patientPresenter.present(patient).orDie
        yield Response.text(encoded)
    }

object PatientApp:
  val live: ZLayer[PatientService & PatientPresenter, Nothing, PatientApp] =
    ZLayer.fromFunction(PatientApp(_, _))

  def http: HttpApp[PatientApp, NoSuchElementException] =
    Http.fromZIO(ZIO.service[PatientApp].map(_.http)).flatten

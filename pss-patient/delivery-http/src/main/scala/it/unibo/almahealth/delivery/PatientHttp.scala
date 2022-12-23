package it.unibo.almahealth.delivery

import ca.uhn.fhir.context.FhirContext
import it.unibo.almahealth.domain.Identifier
import it.unibo.almahealth.presenter.PatientPresenter
import it.unibo.almahealth.presenter.Presenter
import it.unibo.almahealth.repository.PatientRepository
import it.unibo.almahealth.service.PatientService
import org.hl7.fhir.r4.model.AllergyIntolerance
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.MedicationStatement
import zio.ZIO
import zio.ZLayer
import zio.http.*
import zio.http.model.Method

type BundlePresenter = Presenter[Bundle, String]

class PatientApp(
    patientService: PatientService,
    bundlePresenter: BundlePresenter
):
  def http: HttpApp[Any, NoSuchElementException] =
    Http.collectZIO[Request] {
      case Method.GET -> !! / identifier / "allergyIntolerances" =>
        for
          allergies <- patientService
            .patient(Identifier(identifier))
            .flatMap(_.allergyIntolerances)
          encoded <- bundlePresenter.present(allergies).orDie
        yield Response.text(encoded)
      case Method.GET -> !! / identifier / "medications" =>
        for
          medicationStatements <- patientService
            .patient(Identifier(identifier))
            .flatMap(_.medications)
          encoded <- bundlePresenter.present(medicationStatements).orDie
        yield Response.text(encoded)

    }

object PatientApp:
  val live: ZLayer[PatientService & BundlePresenter, Nothing, PatientApp] =
    ZLayer.fromFunction(PatientApp(_, _))

  def http: HttpApp[PatientApp, NoSuchElementException] =
    Http.fromZIO(ZIO.service[PatientApp].map(_.http)).flatten

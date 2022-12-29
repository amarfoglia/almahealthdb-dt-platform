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
import org.hl7.fhir.r4.model.Patient
import zio.ZIO
import zio.ZLayer
import zio.http.*
import zio.http.model.Method
import it.unibo.almahealth.context.ZFhirContext
import ca.uhn.fhir.parser.DataFormatException

type PatientPresenter = Presenter[Patient, String]
type BundlePresenter  = Presenter[Bundle, String]

class PatientApp(
    patientService: PatientService,
    zFhirContext: ZFhirContext,
    patientPresenter: PatientPresenter,
    bundlePresenter: BundlePresenter
):
  def http: HttpApp[Any, NoSuchElementException | DataFormatException] =
    Http.collectZIO[Request] {
      case Method.GET -> !! / identifier =>
        for
          patient <- patientService.patient(Identifier(identifier)).flatMap(_.get)
          encoded <- patientPresenter.present(patient).orDie
        yield Response.text(encoded)
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
      case Method.GET -> !! / identifier / "problems" =>
        for
          problems <- patientService
            .patient(Identifier(identifier))
            .flatMap(_.problems)
          encoded <- bundlePresenter.present(problems).orDie
        yield Response.text(encoded)
      case Method.GET -> !! / identifier / "medicalEquipment" =>
        for
          medicalEquipment <- patientService
            .patient(Identifier(identifier))
            .flatMap(_.medicalEquipment)
          encoded <- bundlePresenter.present(medicalEquipment).orDie
        yield Response.text(encoded)
      case Method.GET -> !! / identifier / "procedures" =>
        for
          procedures <- patientService
            .patient(Identifier(identifier))
            .flatMap(_.procedures)
          encoded <- bundlePresenter.present(procedures).orDie
        yield Response.text(encoded)
      case Method.GET -> !! / identifier / "functionalStatus" =>
        for
          functionalStatus <- patientService
            .patient(Identifier(identifier))
            .flatMap(_.functionalStatus)
          encoded <- bundlePresenter.present(functionalStatus).orDie
        yield Response.text(encoded)
      case Method.GET -> !! / identifier / "immunizations" =>
        for
          immunizations <- patientService
            .patient(Identifier(identifier))
            .flatMap(_.immunizations)
          encoded <- bundlePresenter.present(immunizations).orDie
        yield Response.text(encoded)
      case Method.GET -> !! / identifier / "socialHistory" =>
        for
          socialHistory <- patientService
            .patient(Identifier(identifier))
            .flatMap(_.socialHistory)
          encoded <- bundlePresenter.present(socialHistory).orDie
        yield Response.text(encoded)
      case Method.GET -> !! / identifier / "vitalSigns" =>
        for
          vitalSigns <- patientService
            .patient(Identifier(identifier))
            .flatMap(_.vitalSigns)
          encoded <- bundlePresenter.present(vitalSigns).orDie
        yield Response.text(encoded)
      case req @ Method.POST -> !! / "uploadDocument" =>
        for
          body   <- req.body.asString.mapError(_ => new DataFormatException("Couldn't parse body"))
          parser <- zFhirContext.newJsonParser
          bundle <- parser.parseString(classOf[Bundle], body)
          _      <- patientService.uploadDocument(bundle)
        yield Response.ok
    }

object PatientApp:
  private type Deps = PatientService & ZFhirContext & PatientPresenter & BundlePresenter
  val live: ZLayer[Deps, Nothing, PatientApp] =
    ZLayer.fromFunction(PatientApp(_, _, _, _))

  def http: HttpApp[PatientApp, NoSuchElementException | DataFormatException] =
    Http.fromZIO(ZIO.service[PatientApp].map(_.http)).flatten

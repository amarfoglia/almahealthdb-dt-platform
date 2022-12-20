package it.unibo.almahealth.presenter

import zio.UIO
import zio.ZIO
import org.hl7.fhir.r4.model.Patient
import ca.uhn.fhir.parser.DataFormatException
import zio.ZLayer
import it.unibo.almahealth.context.ZFhirContext

type PatientPresenter = Presenter[Patient, String]

class JsonPatientPresenter(context: ZFhirContext) extends PatientPresenter:
  override def present(b: Patient): ZIO[Any, PresenterException, String] =
    context.newJsonEncoder.flatMap {
      _.encodeResourceToString(b)
        .mapError(_.getMessage)
        .mapError(PresenterException(_))
    }

object PatientPresenter:
  def json: ZLayer[ZFhirContext, Nothing, JsonPatientPresenter] =
    ZLayer.fromFunction(JsonPatientPresenter(_))

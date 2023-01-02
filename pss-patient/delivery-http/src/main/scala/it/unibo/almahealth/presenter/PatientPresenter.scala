package it.unibo.almahealth.presenter

import ca.uhn.fhir.parser.DataFormatException
import it.unibo.almahealth.context.ZFhirContext
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Resource
import zio.UIO
import zio.ZIO
import zio.ZLayer

type ResourcePresenter = Presenter[Resource, String]

class JsonResourcePresenter(context: ZFhirContext) extends ResourcePresenter:
  override def present(b: Resource): ZIO[Any, PresenterException, String] =
    context.newJsonEncoder.flatMap {
      _.encodeResourceToString(b)
        .mapError(_.getMessage)
        .mapError(PresenterException(_))
    }

object ResourcePresenter:
  def json: ZLayer[ZFhirContext, Nothing, JsonResourcePresenter] =
    ZLayer.fromFunction(JsonResourcePresenter(_))

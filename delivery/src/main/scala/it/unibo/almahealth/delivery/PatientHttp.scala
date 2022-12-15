package it.unibo.almahealth.delivery

import it.unibo.almahealth.usecases.PatientUseCases
import it.unibo.almahealth.domain.syntax.pattern.*
import it.unibo.almahealth.context.ZFhirContext
import zio.http.*
import zio.http.model.Method
import zio.ZIO
import ca.uhn.fhir.context.FhirContext
import it.unibo.almahealth.repository.PatientRepository

def patientHttp: Http[PatientRepository & FhirContext, NoSuchElementException, Request, Response] = 
  Http.collectZIO[Request] {
    case Method.GET -> !! / Identifier(identifier) => 
      for
        patient <- PatientUseCases.findById(identifier)
        parser <- ZFhirContext.newJsonParser
      yield Response.text(parser.encodeResourceToString(patient))
  }
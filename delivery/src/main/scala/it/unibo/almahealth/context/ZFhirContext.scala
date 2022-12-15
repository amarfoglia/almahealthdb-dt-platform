package it.unibo.almahealth.context

import ca.uhn.fhir.context.FhirContext
import zio.URIO
import zio.ZIO
import zio.ZLayer
import ca.uhn.fhir.parser.IParser

object ZFhirContext:
  object live:
    def forR4: ZLayer[Any, Nothing, FhirContext] = 
      ZLayer.succeed(FhirContext.forR4())

  def newJsonParser: URIO[FhirContext, IParser] = 
    ZIO.serviceWith[FhirContext](_.newJsonParser())
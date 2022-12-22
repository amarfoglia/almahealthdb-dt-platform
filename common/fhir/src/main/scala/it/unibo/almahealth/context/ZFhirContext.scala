package it.unibo.almahealth.context

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.parser.IParser
import zio.UIO
import zio.IO
import zio.URIO
import zio.ZIO
import zio.ZLayer
import org.hl7.fhir.r4.model.Resource
import java.io.IOException
import ca.uhn.fhir.parser.DataFormatException
import org.hl7.fhir.r4.model.BaseResource

/**
 * Wrapper for the [[ca.uhn.fhir.context.FhirContext]] class that is side-effect free.
 */
class ZFhirContext(ctx: FhirContext):
  def newJsonParser: UIO[ZParser] =
    ZIO.succeed(ctx.newJsonParser()).map(ZParser(_))

  def newJsonEncoder: UIO[ZEncoder] =
    ZIO.succeed(ctx.newJsonParser()).map(ZEncoder(_))

object ZFhirContext:
  object live:
    def forR4: ZLayer[Any, Nothing, ZFhirContext] =
      ZLayer.succeed(ZFhirContext(FhirContext.forR4()))

  def newJsonParser: URIO[ZFhirContext, ZParser] =
    ZIO.serviceWithZIO[ZFhirContext](_.newJsonParser)

  def newJsonEncoder: URIO[ZFhirContext, ZEncoder] =
    ZIO.serviceWithZIO[ZFhirContext](_.newJsonEncoder)


/**
 * Wrapper for the [[ca.uhn.fhir.parser.IParser]] class that is side-effect free.
 */
class ZEncoder(parser: IParser):
  def encodeResourceToString(resource: Resource): IO[DataFormatException, String] =
    ZIO.attempt {
      parser.encodeResourceToString(resource)
    }.refineToOrDie[DataFormatException]

class ZParser(parser: IParser):
  def parseString[A <: BaseResource](cls: Class[A], in: String): IO[DataFormatException, A] =
    ZIO.attempt {
      parser.parseResource(cls, in)
    }.refineToOrDie[DataFormatException]

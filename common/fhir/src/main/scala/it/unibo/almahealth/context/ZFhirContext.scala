package it.unibo.almahealth.context

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.parser.DataFormatException
import ca.uhn.fhir.parser.IParser
import org.hl7.fhir.r4.model.BaseResource
import org.hl7.fhir.r4.model.Resource
import zio.IO
import zio.UIO
import zio.URIO
import zio.ZIO
import zio.ZLayer

import java.io.IOException
import scala.jdk.javaapi.CollectionConverters

import util.chaining.*

/** Wrapper for the [[ca.uhn.fhir.context.FhirContext]] class that is side-effect free.
  */
class ZFhirContext(ctx: FhirContext):
  def newJsonParser: UIO[ZParser] =
    ZIO.succeed(ctx.newJsonParser()).map(ZParser(_))

  def newJsonEncoder: UIO[ZEncoder] =
    ZIO.succeed(ctx.newJsonParser()).map(ZEncoder(_))

  def newRDFParser: UIO[ZParser] =
    ZIO.succeed(ctx.newRDFParser()).map(ZParser(_))

  def newRDFEncoder: UIO[ZEncoder] =
    ZIO.succeed(ctx.newRDFParser()).map(ZEncoder(_))

object ZFhirContext:
  object live:
    def forR4: ZLayer[Any, Nothing, ZFhirContext] =
      ZLayer.succeed(ZFhirContext(FhirContext.forR4()))

  def newJsonParser: URIO[ZFhirContext, ZParser] =
    ZIO.serviceWithZIO[ZFhirContext](_.newJsonParser)

  def newJsonEncoder: URIO[ZFhirContext, ZEncoder] =
    ZIO.serviceWithZIO[ZFhirContext](_.newJsonEncoder)

/** Wrapper for the [[ca.uhn.fhir.parser.IParser]] class that is side-effect free.
  */
class ZEncoder(
  parser: IParser,
  private val dontEncodeElements: Set[String] = Set()
):
  def setDontEncodeElements(elem: String, rest: String*): ZEncoder = 
    setDontEncodeElements(rest.toSet + elem)

  def setDontEncodeElements(elems: Set[String]): ZEncoder = 
    new ZEncoder(parser, dontEncodeElements ++ elems)

  def encodeResourceToString(resource: Resource): IO[DataFormatException, String] =
    ZIO
      .attempt {
        parser.setDontEncodeElements(CollectionConverters.asJava(dontEncodeElements))
        val prefixMatcher = "@prefix.+".r
        val nodeRoleMatcher = raw"fhir:nodeRole\s+fhir:treeRoot\s+.".r
        parser.encodeResourceToString(resource)
          .pipe(prefixMatcher.replaceAllIn(_, ""))
          .pipe(nodeRoleMatcher.replaceAllIn(_, ""))
      }
      .refineToOrDie[DataFormatException]

class ZParser(parser: IParser):
  def parseString[A <: BaseResource](cls: Class[A], in: String): IO[DataFormatException, A] =
    ZIO
      .attempt {
        parser.parseResource(cls, in)
      }
      .refineToOrDie[DataFormatException]

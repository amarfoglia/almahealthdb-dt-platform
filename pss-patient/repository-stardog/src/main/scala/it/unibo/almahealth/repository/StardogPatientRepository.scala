package it.unibo.almahealth.repository

import com.complexible.common.base.Options
import com.complexible.common.rdf.query.resultio.TextTableQueryResultWriter
import com.complexible.stardog.api.ConnectionPool
import com.stardog.stark.Namespaces
import it.unibo.almahealth.Namespaces as FhirNamespaces
import com.stardog.stark.Statement
import com.stardog.stark.Values
import com.stardog.stark.io.turtle.TurtleWriter
import com.stardog.stark.io.turtle.TurtleWriter.TurtleWriterFactory
import com.stardog.stark.query.GraphQueryResult
import com.stardog.stark.query.SelectQueryResult
import com.stardog.stark.query.io.QueryResultWriters
import it.unibo.almahealth.context.ZFhirContext
import it.unibo.almahealth.domain.Identifier
import it.unibo.almahealth.stardog.ZConnection.Namespace
import it.unibo.almahealth.stardog.ZConnection.Parameter
import it.unibo.almahealth.stardog.ZConnectionPool
import it.unibo.almahealth.stardog.ZTurtleWriter
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Patient
import zio.ZIO
import zio.stream.ZSink
import zio.stream.ZStream

import scala.jdk.StreamConverters._

class StardogPatientRepository(
    zConnectionPool: ZConnectionPool,
    zFhirContext: ZFhirContext,
    zTurtleWriter: ZTurtleWriter
) extends PatientRepository:
  import syntax.*

  override def findById(
      identifier: Identifier
  ): ZIO[Any, NoSuchPatientException, Patient] =
    zConnectionPool
      .withConnection { conn =>
        for
          serialized <- conn
            .addNamespace(Namespace("fhir", FhirNamespaces.FHIR))
            .graph(
              Queries.findById,
              parameters = List(Parameter("identifier", identifier.value))
            )
            .mapZIO(zTurtleWriter.write(_))
            .run(ZSink.mkString)
            .orDie
          _ <-
            if serialized == "" then
              ZIO.fail(
                new NoSuchPatientException(
                  s"There is no such patient with id = ${identifier.value}"
                )
              )
            else ZIO.unit
          parser  <- zFhirContext.newRDFParser
          patient <- parser.parseString(classOf[Patient], serialized).orDie
        yield patient
      }
      .refineToOrDie[NoSuchPatientException]

  override def getMedications(identifier: Identifier): ZIO[Any, NoSuchPatientException, Bundle] =
    getResource(identifier, "MedicationStatement", "10160-0")

  override def getSocialHistory(identifier: Identifier): ZIO[Any, NoSuchPatientException, Bundle] =
    getResource(identifier, "Observation", "29762-2")

  override def getAllergyIntolerances(
      identifier: Identifier
  ): ZIO[Any, NoSuchPatientException, Bundle] =
    getResource(identifier, "AllergyIntolerance", "48765-2")

  override def getProblems(identifier: Identifier): ZIO[Any, NoSuchPatientException, Bundle] =
    getResource(identifier, "Condition", "11450-4")

  override def getVitalSigns(identifier: Identifier): ZIO[Any, NoSuchPatientException, Bundle] =
    getResource(identifier, "Observation", "8716-3")

  override def getProcedures(identifier: Identifier): ZIO[Any, NoSuchPatientException, Bundle] =
    getResource(identifier, "Procedure", "47519-4")

  override def getImmunizations(identifier: Identifier): ZIO[Any, NoSuchPatientException, Bundle] =
    getResource(identifier, "Immunization", "11369-6")

  override def getFunctionalStatus(
      identifier: Identifier
  ): ZIO[Any, NoSuchPatientException, Bundle] =
    getResource(identifier, "Observation", "47420-5")

  override def getMedicalEquipment(
      identifier: Identifier
  ): ZIO[Any, NoSuchPatientException, Bundle] =
    getResource(identifier, "Device", "46264-8")

  override def uploadDocument(document: Bundle): ZIO[Any, Nothing, Unit] =
    zConnectionPool.withConnection { conn =>
      for
        encoder    <- zFhirContext.newRDFEncoder
        serialized <- encoder.encodeResourceToString(document).orDie.debug
        _ <- conn
          .update(s"""
INSERT DATA { ${serialized} }
""").orDie
      yield ()
    }

  private def getResource(
      identifier: Identifier,
      fhirResourceName: String,
      sectionLoincCode: String
  ): ZIO[Any, NoSuchPatientException, Bundle] =
    zConnectionPool.withConnection { conn =>
      val statements = ZStream
        .succeed(Values.bnode())
        .flatMap { bundle =>
          ZStream(
            Values
              .statement(
                bundle,
                Values.iri(Namespaces.RDF, "type"),
                Values.iri(FhirNamespaces.FHIR, "Bundle")
              ),
            Values.statement(
              bundle,
              Values.iri(FhirNamespaces.FHIR, "nodeRole"),
              Values.iri(FhirNamespaces.FHIR, "treeRoot")
            )
          ) ++ conn
            .addNamespace(Namespace("fhir", FhirNamespaces.FHIR))
            .graph(
              Queries.makeResource(fhirResourceName, sectionLoincCode),
              parameters = List(Parameter("identifier", identifier.value))
            )
            .orDie
            .mergeIf(s =>
              s.predicate.toString == Namespaces.RDF + "type" && s.`object`.toString == FhirNamespaces.FHIR + fhirResourceName
            ) { s =>
              ZStream.succeed(Values.bnode()).flatMap { bundleEntry =>
                ZStream(
                  Values.statement(
                    bundle,
                    Values.iri(FhirNamespaces.FHIR, "Bundle.entry"),
                    bundleEntry
                  ),
                  Values
                    .statement(
                      bundleEntry,
                      Values.iri(FhirNamespaces.FHIR, "Bundle.entry.resource"),
                      s.subject()
                    )
                )
              }
            }
        }
        .runCollect
        .filterOrElse(_.size > 2)(ZIO.fail(new NoSuchElementException()))

      for
        serialized <- ZStream
          .fromZIO(statements)
          .flattenChunks
          .mapZIO(zTurtleWriter.write(_))
          .run(ZSink.mkString)
        parser <- zFhirContext.newRDFParser
        bundle <- parser.parseString(classOf[Bundle], serialized).orDie
      yield bundle

    }

  private object syntax:
    extension [R, E, A](zstream: ZStream[R, E, A])
      def mergeIf[R1 <: R, E1 >: E, A1 >: A](pred: A => Boolean)(
          toMerge: A => ZStream[R1, E1, A1]
      ): ZStream[R1, E1, A1] = zstream.flatMap { a =>
        if pred(a) then ZStream.succeed(a) ++ toMerge(a)
        else ZStream.succeed(a)
      }

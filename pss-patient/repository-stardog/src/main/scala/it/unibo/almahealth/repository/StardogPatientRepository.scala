package it.unibo.almahealth.repository

import scala.jdk.StreamConverters._
import com.stardog.stark.query.io.QueryResultWriters;
import com.complexible.common.rdf.query.resultio.TextTableQueryResultWriter;
import com.complexible.stardog.api.ConnectionPool
import com.stardog.stark.query.GraphQueryResult
import it.unibo.almahealth.domain.Identifier
import it.unibo.almahealth.stardog.ZConnectionPool
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Patient
import zio.ZIO
import zio.stream.ZStream
import com.stardog.stark.Namespaces
import com.stardog.stark.io.turtle.TurtleWriter.TurtleWriterFactory
import com.stardog.stark.io.turtle.TurtleWriter
import com.complexible.common.base.Options
import it.unibo.almahealth.stardog.ZTurtleWriter
import it.unibo.almahealth.stardog.ZConnection.Namespace
import it.unibo.almahealth.stardog.ZConnection.Parameter
import com.stardog.stark.query.SelectQueryResult
import it.unibo.almahealth.context.ZFhirContext
import zio.stream.ZSink

class StardogPatientRepository(
    zConnectionPool: ZConnectionPool,
    zFhirContext: ZFhirContext
) extends PatientRepository {

  lazy val writer = ZTurtleWriter()

  private val findByIdQuery = """
  | CONSTRUCT { ?s ?p ?o }
  | WHERE {
  |     ?patient a fhir:Patient ;
  |                 fhir:Patient.identifier  [
  |                     # fhir:Identifier.assigner / fhir:Reference.display / fhir:value  "MEF" ;
  |                     fhir:Identifier.system   / fhir:value  "http://terminology.hl7.it/CodeSystem/it-tipoEntita/mef"^^xsd:anyURI ;
  |                     fhir:Identifier.value    / fhir:value  ?identifier ;
  |                 ] .
  |
  |     ?patient (<>|!<>)* ?s .
  |     ?s ?p ?o .
  | }""".stripMargin

//   def test: ZIO[Any, Nothing, Unit] =
//     zConnectionPool.withConnection { conn =>
//       conn.connection.namespaces().add("fhir", "http://hl7.org/fhir/")
//       val query = """
//       |DESCRIBE ?p
//       |WHERE { ?p a fhir:Patient }
// """.stripMargin
//       val res = conn.connection
//         .graph(
//           findByIdQuery,
//           Namespaces.STARDOG
//         )
//         // .parameter("identifier", "GTWGWY82B42G920M")
//         .execute()
//       val zwriter = ZTurtleWriter()
//       zwriter
//         .write(res.stream().toScala(LazyList))
//         .flatMap(ZIO.debug(_))
//       // val writer = TurtleWriter.TurtleWriterFactory().create(System.out, Options.empty())
//       // writer.start()
//       // while (res.hasNext()) {
//       //   val s = res.next
//       //   writer.handle(s)
//       // }
//       // writer.end()

//       // ZIO.unit
//       // ZStream
//       //   .fromJavaStream(res.stream())
//       //   .map { s => s"${s.subject} ${s.predicate} ${s.`object`()}." }
//       //   .foreach(ZIO.debug(_))
//       // .runCollect
//     }.orDie

  override def findById(
      identifier: Identifier
  ): ZIO[Any, NoSuchPatientException, Patient] =
    zConnectionPool
      .withConnection { conn =>
        for
          serialized <- conn
            .addNamespace(Namespace("fhir", "http://hl7.org/fhir/"))
            .graph(findByIdQuery, parameters = List(Parameter("identifier", identifier.value)))
            .mapZIO(writer.write(_))
            .run(ZSink.mkString)
            .map(
              _ + "\n"
                + "<urn:uuid:6d5799c8-2be2-39fc-a719-80a66c1b311c> <http://hl7.org/fhir/nodeRole> <http://hl7.org/fhir/treeRoot> ."
            )
            .debug
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
    ???

  override def getSocialHistory(identifier: Identifier): ZIO[Any, NoSuchPatientException, Bundle] =
    ???

  override def getAllergyIntolerances(
      identifier: Identifier
  ): ZIO[Any, NoSuchPatientException, Bundle] = ???

  override def getProblems(identifier: Identifier): ZIO[Any, NoSuchPatientException, Bundle] = ???

  override def getVitalSigns(identifier: Identifier): ZIO[Any, NoSuchPatientException, Bundle] = ???

  override def getProcedures(identifier: Identifier): ZIO[Any, NoSuchPatientException, Bundle] = ???

  override def getImmunizations(identifier: Identifier): ZIO[Any, NoSuchPatientException, Bundle] =
    ???

  override def getFunctionalStatus(
      identifier: Identifier
  ): ZIO[Any, NoSuchPatientException, Bundle] = ???

  override def getMedicalEquipment(
      identifier: Identifier
  ): ZIO[Any, NoSuchPatientException, Bundle] = ???

}

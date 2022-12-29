package it.unibo.almahealth.repository

import it.unibo.almahealth.domain.Identifier
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Patient
import zio.ZIO
import zio.ZLayer

class InMemoryPatientRepository(
    private val patients: Map[Identifier, Patient]
) extends PatientRepository:

  override def uploadDocument(document: Bundle): ZIO[Any, Nothing, Unit] = ???

  override def findById(identifier: Identifier): ZIO[Any, NoSuchElementException, Patient] =
    ZIO
      .attempt {
        patients.get(identifier).get
      }
      .refineToOrDie[NoSuchElementException]

  override def getAllergyIntolerances(
      identifier: Identifier
  ): ZIO[Any, NoSuchElementException, Bundle] = ???

  override def getMedications(
      identifier: Identifier
  ): ZIO[Any, NoSuchElementException, Bundle] = ???

  override def getSocialHistory(identifier: Identifier): ZIO[Any, NoSuchPatientException, Bundle] =
    ???

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

object InMemoryPatientRepository:
  def live(patients: Map[Identifier, Patient]): ZLayer[Any, Nothing, PatientRepository] =
    ZLayer.succeed(InMemoryPatientRepository(patients))

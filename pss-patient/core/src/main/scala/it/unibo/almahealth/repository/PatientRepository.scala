package it.unibo.almahealth.repository

import it.unibo.almahealth.domain.Identifier
import org.hl7.fhir.r4.model.AllergyIntolerance
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Patient
import zio.ZIO
import zio.ZLayer

type NoSuchPatientException = NoSuchElementException

trait PatientRepository:

  def findById(identifier: Identifier): ZIO[Any, NoSuchPatientException, Patient]

  def getAllergyIntolerances(identifier: Identifier): ZIO[Any, NoSuchPatientException, Bundle]

  def getMedications(identifier: Identifier): ZIO[Any, NoSuchPatientException, Bundle]

  def getProblems(identifier: Identifier): ZIO[Any, NoSuchPatientException, Bundle]

  def getMedicalEquipment(identifier: Identifier): ZIO[Any, NoSuchPatientException, Bundle]

  def getProcedures(identifier: Identifier): ZIO[Any, NoSuchPatientException, Bundle]

  def getFunctionalStatus(identifier: Identifier): ZIO[Any, NoSuchPatientException, Bundle]

  def getImmunizations(identifier: Identifier): ZIO[Any, NoSuchPatientException, Bundle]

  def getSocialHistory(identifier: Identifier): ZIO[Any, NoSuchPatientException, Bundle]

  def getVitalSigns(identifier: Identifier): ZIO[Any, NoSuchPatientException, Bundle]

  def uploadDocument(document: Bundle): ZIO[Any, Nothing, Unit]

object PatientRepository:

  def findById(identifier: Identifier): ZIO[PatientRepository, NoSuchElementException, Patient] =
    ZIO.serviceWithZIO[PatientRepository](_.findById(identifier))

  def getAllergyIntolerances(
      identifier: Identifier
  ): ZIO[PatientRepository, NoSuchPatientException, Bundle] =
    ZIO.serviceWithZIO[PatientRepository](_.getAllergyIntolerances(identifier))

  def getMedications(
      identifier: Identifier
  ): ZIO[PatientRepository, NoSuchPatientException, Bundle] =
    ZIO.serviceWithZIO[PatientRepository](_.getMedications(identifier))

  def getProblems(identifier: Identifier): ZIO[PatientRepository, NoSuchPatientException, Bundle] =
    ZIO.serviceWithZIO[PatientRepository](_.getProblems(identifier))

  def getMedicalEquipment(
      identifier: Identifier
  ): ZIO[PatientRepository, NoSuchPatientException, Bundle] =
    ZIO.serviceWithZIO[PatientRepository](_.getMedicalEquipment(identifier))

  def getProcedures(
      identifier: Identifier
  ): ZIO[PatientRepository, NoSuchPatientException, Bundle] =
    ZIO.serviceWithZIO[PatientRepository](_.getProcedures(identifier))

  def getFunctionalStatus(
      identifier: Identifier
  ): ZIO[PatientRepository, NoSuchPatientException, Bundle] =
    ZIO.serviceWithZIO[PatientRepository](_.getFunctionalStatus(identifier))

  def getImmunizations(
      identifier: Identifier
  ): ZIO[PatientRepository, NoSuchPatientException, Bundle] =
    ZIO.serviceWithZIO[PatientRepository](_.getImmunizations(identifier))

  def getSocialHistory(
      identifier: Identifier
  ): ZIO[PatientRepository, NoSuchPatientException, Bundle] =
    ZIO.serviceWithZIO[PatientRepository](_.getSocialHistory(identifier))

  def getVitalSigns(
      identifier: Identifier
  ): ZIO[PatientRepository, NoSuchPatientException, Bundle] =
    ZIO.serviceWithZIO[PatientRepository](_.getVitalSigns(identifier))

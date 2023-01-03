package it.unibo.almahealth.repository

import it.unibo.almahealth.domain.Identifier
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Patient
import zio.URLayer
import zio.ZIO
import zio.ZLayer
import zio.mock.Mock
import zio.mock.Proxy

object MockPatientRepository extends Mock[PatientRepository]:
  object FindById               extends Effect[Identifier, NoSuchPatientException, Patient]
  object GetAllergyIntolerances extends Effect[Identifier, NoSuchPatientException, Bundle]
  object GetFunctionalStatus    extends Effect[Identifier, NoSuchPatientException, Bundle]
  object GetImmunizations       extends Effect[Identifier, NoSuchPatientException, Bundle]
  object GetMedicalEquipment    extends Effect[Identifier, NoSuchPatientException, Bundle]
  object GetMedications         extends Effect[Identifier, NoSuchPatientException, Bundle]
  object GetProblems            extends Effect[Identifier, NoSuchPatientException, Bundle]
  object GetProcedures          extends Effect[Identifier, NoSuchPatientException, Bundle]
  object GetSocialHistory       extends Effect[Identifier, NoSuchPatientException, Bundle]
  object GetVitalSigns          extends Effect[Identifier, NoSuchPatientException, Bundle]
  object UploadDocument         extends Effect[Bundle, Nothing, Unit]

  val compose: URLayer[Proxy, PatientRepository] = ZLayer {
    for proxy <- ZIO.service[Proxy]
    yield new PatientRepository:
      override def getProcedures(
          identifier: Identifier
      ): ZIO[Any, NoSuchPatientException, Bundle] = proxy(GetProcedures, identifier)

      override def getMedications(
          identifier: Identifier
      ): ZIO[Any, NoSuchPatientException, Bundle] = proxy(GetMedications, identifier)

      override def getMedicalEquipment(
          identifier: Identifier
      ): ZIO[Any, NoSuchPatientException, Bundle] = proxy(GetMedicalEquipment, identifier)

      override def getFunctionalStatus(
          identifier: Identifier
      ): ZIO[Any, NoSuchPatientException, Bundle] = proxy(GetFunctionalStatus, identifier)

      override def getSocialHistory(
          identifier: Identifier
      ): ZIO[Any, NoSuchPatientException, Bundle] = proxy(GetSocialHistory, identifier)

      override def uploadDocument(document: Bundle): ZIO[Any, Nothing, Unit] =
        proxy(UploadDocument, document)

      override def getProblems(identifier: Identifier): ZIO[Any, NoSuchPatientException, Bundle] =
        proxy(GetProblems, identifier)

      override def getVitalSigns(
          identifier: Identifier
      ): ZIO[Any, NoSuchPatientException, Bundle] = proxy(GetVitalSigns, identifier)

      override def getImmunizations(
          identifier: Identifier
      ): ZIO[Any, NoSuchPatientException, Bundle] = proxy(GetImmunizations, identifier)

      override def findById(identifier: Identifier): ZIO[Any, NoSuchPatientException, Patient] =
        proxy(FindById, identifier)

      override def getAllergyIntolerances(
          identifier: Identifier
      ): ZIO[Any, NoSuchPatientException, Bundle] =
        proxy(GetAllergyIntolerances, identifier)
  }

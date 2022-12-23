package it.unibo.almahealth.service

import it.unibo.almahealth.domain.Identifier
import it.unibo.almahealth.repository.PatientRepository
import org.hl7.fhir.r4.model.AllergyIntolerance
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.MedicationStatement
import org.hl7.fhir.r4.model.Patient
import zio.IO
import zio.ZIO
import zio.UIO
import zio.ZLayer

import PatientService.Builder

class PatientService(patientRepository: PatientRepository):

  def patient(identifier: Identifier): ZIO[Any, NoSuchElementException, Builder] =
    patientRepository
      .findById(identifier)
      .map(_ => Builder(identifier, patientRepository))

object PatientService:

  final case class Builder(
      private val identifier: Identifier,
      private val repository: PatientRepository
  ):
    def allergyIntolerances: UIO[Bundle] = repository.getAllergyIntolerances(identifier).orDie

    def medications: UIO[Bundle] = repository.getMedications(identifier).orDie

    def problems: UIO[Bundle] = repository.getProblems(identifier).orDie

    def medicalEquipment: UIO[Bundle] = repository.getMedicalEquipment(identifier).orDie

    def procedures: UIO[Bundle] = repository.getProcedures(identifier).orDie

    def functionalStatus: UIO[Bundle] = repository.getFunctionalStatus(identifier).orDie

    def immunizations: UIO[Bundle] = repository.getImmunizations(identifier).orDie

    def socialHistory: UIO[Bundle] = repository.getSocialHistory(identifier).orDie

    def vitalSigns: UIO[Bundle] = repository.getVitalSigns(identifier).orDie

  val live: ZLayer[PatientRepository, Nothing, PatientService] =
    ZLayer.fromFunction(PatientService(_))

  def patient(identifier: Identifier): ZIO[PatientService, NoSuchElementException, Builder] =
    ZIO.serviceWithZIO[PatientService](_.patient(identifier))

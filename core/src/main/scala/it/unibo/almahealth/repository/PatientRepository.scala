package it.unibo.almahealth.repository

import it.unibo.almahealth.domain.Identifier
import zio.ZIO
import org.hl7.fhir.r4.model.Patient
import zio.ZLayer

trait PatientRepository:
  def findById(identifier: Identifier): ZIO[Any, NoSuchElementException, Patient] = ???


object PatientRepository:
  def inMemory(patients: Map[Identifier, Patient]): ZLayer[Any, Nothing, PatientRepository] = 
    ZLayer.succeed(InMemoryPatientRepository(patients))

  def findById(identifier: Identifier): ZIO[PatientRepository, NoSuchElementException, Patient] = 
    ZIO.serviceWithZIO[PatientRepository](_.findById(identifier))
  


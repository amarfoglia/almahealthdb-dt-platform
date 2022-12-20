package it.unibo.almahealth.repository

import it.unibo.almahealth.domain.Identifier

import org.hl7.fhir.r4.model.Patient

import zio.ZIO
import zio.ZLayer

class InMemoryPatientRepository(
  private val patients: Map[Identifier, Patient]
) extends PatientRepository:
  override def findById(identifier: Identifier): ZIO[Any, NoSuchElementException, Patient] = 
    ZIO.attempt {
      patients.get(identifier).get
    }.refineToOrDie[NoSuchElementException]
  

object InMemoryPatientRepository:
  def live(patients: Map[Identifier, Patient]): ZLayer[Any, Nothing, PatientRepository] =
    ZLayer.succeed(InMemoryPatientRepository(patients))

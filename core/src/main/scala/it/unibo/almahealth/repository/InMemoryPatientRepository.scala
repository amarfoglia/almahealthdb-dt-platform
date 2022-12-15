package it.unibo.almahealth.repository

import it.unibo.almahealth.domain.Identifier

import org.hl7.fhir.r4.model.Patient

import zio.ZIO

class InMemoryPatientRepository(
  private val patients: Map[Identifier, Patient]
) extends PatientRepository:
  override def findById(identifier: Identifier): ZIO[Any, NoSuchElementException, Patient] = 
    ZIO.attempt {
      patients.get(identifier).get
    }.refineToOrDie[NoSuchElementException]
  


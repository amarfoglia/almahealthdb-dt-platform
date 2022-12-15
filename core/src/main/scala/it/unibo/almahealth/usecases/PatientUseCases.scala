package it.unibo.almahealth.usecases

import org.hl7.fhir.r4.model.Patient
import zio.IO
import org.hl7.fhir.r4.model.AllergyIntolerance

import it.unibo.almahealth.domain.Identifier
import it.unibo.almahealth.repository.PatientRepository
import zio.ZIO

object PatientUseCases:  
  def findById(identifier: Identifier): ZIO[PatientRepository, NoSuchElementException, Patient] = 
    ZIO.serviceWithZIO[PatientRepository](_.findById(identifier))

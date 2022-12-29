package it.unibo.almahealth.service

import zio.test.ZIOSpecDefault
import zio.mock.Mock
import zio.mock.Expectation
import zio.mock.Proxy
import it.unibo.almahealth.repository.PatientRepository
import it.unibo.almahealth.repository.NoSuchPatientException
import it.unibo.almahealth.domain.Identifier
import org.hl7.fhir.r4.model.Patient
import zio.ZLayer
import zio.ZIO
import zio.URLayer
import zio.test.assertTrue
import org.hl7.fhir.r4.model.Bundle
import zio.test.Assertion
import util.chaining.*
import it.unibo.almahealth.repository.MockPatientRepository
import org.hl7.fhir.r4.model.Bundle.BundleType

object PatientServiceSpec extends ZIOSpecDefault:
  val mario  = new Patient().tap(_.addName().tap(_.addGiven("Mario")))
  val bundle = new Bundle().tap(_.setType(BundleType.DOCUMENT))

  def nonEmptyRepository = MockPatientRepository
    .FindById(
      assertion = Assertion.equalTo(Identifier("some-id")),
      result = Expectation.value(mario)
    )

  def spec = suite("PatientServiceSpec")(
    test("patient.get should return a patient") {
      val program         = PatientService.patient(Identifier("some-id")).flatMap(_.get)
      val mockPatientRepo = nonEmptyRepository.toLayer
      for returned <- program.provide(mockPatientRepo >>> PatientService.live)
      yield assertTrue(returned.equalsDeep(mario))
    },
    test("patient.allergyIntolerances should return a bundle") {
      val program = PatientService.patient(Identifier("some-id")).flatMap(_.allergyIntolerances)
      val mockPatientRepo = (
        nonEmptyRepository ++ MockPatientRepository
          .GetAllergyIntolerances(
            assertion = Assertion.equalTo(Identifier("some-id")),
            result = Expectation.value(bundle)
          )
      ).toLayer
      for returned <- program.provide(mockPatientRepo >>> PatientService.live)
      yield assertTrue(returned.equalsDeep(bundle))
    },
    test("patient.functionalStatus should return a bundle") {
      val program = PatientService.patient(Identifier("some-id")).flatMap(_.functionalStatus)
      val mockPatientRepo = (
        nonEmptyRepository ++ MockPatientRepository
          .GetFunctionalStatus(
            assertion = Assertion.equalTo(Identifier("some-id")),
            result = Expectation.value(bundle)
          )
      ).toLayer
      for returned <- program.provide(mockPatientRepo >>> PatientService.live)
      yield assertTrue(returned.equalsDeep(bundle))
    },
    test("patient.immunizations should return a bundle") {
      val program = PatientService.patient(Identifier("some-id")).flatMap(_.immunizations)
      val mockPatientRepo = (
        nonEmptyRepository ++ MockPatientRepository
          .GetImmunizations(
            assertion = Assertion.equalTo(Identifier("some-id")),
            result = Expectation.value(bundle)
          )
      ).toLayer
      for returned <- program.provide(mockPatientRepo >>> PatientService.live)
      yield assertTrue(returned.equalsDeep(bundle))
    },
    test("patient.medicalEquipment should return a bundle") {
      val program = PatientService.patient(Identifier("some-id")).flatMap(_.medicalEquipment)
      val mockPatientRepo = (
        nonEmptyRepository ++ MockPatientRepository
          .GetMedicalEquipment(
            assertion = Assertion.equalTo(Identifier("some-id")),
            result = Expectation.value(bundle)
          )
      ).toLayer
      for returned <- program.provide(mockPatientRepo >>> PatientService.live)
      yield assertTrue(returned.equalsDeep(bundle))
    },
    test("patient.medications should return a bundle") {
      val program = PatientService.patient(Identifier("some-id")).flatMap(_.medications)
      val mockPatientRepo = (
        nonEmptyRepository ++ MockPatientRepository
          .GetMedications(
            assertion = Assertion.equalTo(Identifier("some-id")),
            result = Expectation.value(bundle)
          )
      ).toLayer
      for returned <- program.provide(mockPatientRepo >>> PatientService.live)
      yield assertTrue(returned.equalsDeep(bundle))
    },
    test("patient.problems should return a bundle") {
      val program = PatientService.patient(Identifier("some-id")).flatMap(_.problems)
      val mockPatientRepo = (
        nonEmptyRepository ++ MockPatientRepository
          .GetProblems(
            assertion = Assertion.equalTo(Identifier("some-id")),
            result = Expectation.value(bundle)
          )
      ).toLayer
      for returned <- program.provide(mockPatientRepo >>> PatientService.live)
      yield assertTrue(returned.equalsDeep(bundle))
    },
    test("patient.procedures should return a bundle") {
      val program = PatientService.patient(Identifier("some-id")).flatMap(_.procedures)
      val mockPatientRepo = (
        nonEmptyRepository ++ MockPatientRepository
          .GetProcedures(
            assertion = Assertion.equalTo(Identifier("some-id")),
            result = Expectation.value(bundle)
          )
      ).toLayer
      for returned <- program.provide(mockPatientRepo >>> PatientService.live)
      yield assertTrue(returned.equalsDeep(bundle))
    },
    test("patient.socialHistory should return a bundle") {
      val program = PatientService.patient(Identifier("some-id")).flatMap(_.socialHistory)
      val mockPatientRepo = (
        nonEmptyRepository ++ MockPatientRepository
          .GetSocialHistory(
            assertion = Assertion.equalTo(Identifier("some-id")),
            result = Expectation.value(bundle)
          )
      ).toLayer
      for returned <- program.provide(mockPatientRepo >>> PatientService.live)
      yield assertTrue(returned.equalsDeep(bundle))
    },
    test("patient.vitalSigns should return a bundle") {
      val program = PatientService.patient(Identifier("some-id")).flatMap(_.vitalSigns)
      val mockPatientRepo = (
        nonEmptyRepository ++ MockPatientRepository
          .GetVitalSigns(
            assertion = Assertion.equalTo(Identifier("some-id")),
            result = Expectation.value(bundle)
          )
      ).toLayer
      for returned <- program.provide(mockPatientRepo >>> PatientService.live)
      yield assertTrue(returned.equalsDeep(bundle))
    },
    test("uploadBundle should return unit") {
      val program = PatientService.uploadDocument(bundle)
      val mockPatientRepo = MockPatientRepository
        .UploadDocument(assertion = Assertion.equalTo(bundle))
        .toLayer

      for _ <- program.provide(mockPatientRepo >>> PatientService.live)
      yield assertTrue(true)
    }
  )

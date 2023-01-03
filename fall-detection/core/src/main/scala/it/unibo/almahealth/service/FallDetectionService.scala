package it.unibo.almahealth.service

import it.unibo.almahealth.context.Topics.Event
import it.unibo.almahealth.context.Topics.syntax.*
import it.unibo.almahealth.domain.Chance
import it.unibo.almahealth.domain.Identifier
import it.unibo.almahealth.events.EventInputPort
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.RiskAssessment
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.stream.ZStream

trait Registry:
  def patient: ZIO[Any, Nothing, Patient]

trait FractureModel[-B]:
  def compute(input: B): ZIO[Any, Nothing, Chance]

class FallDetectionService(
    eventInputPort: EventInputPort[Observation],
    pssRegistry: Registry,
    fractureModel: FractureModel[Observation]
):
  def fallStreamByIdentifier(identifier: Identifier): ZStream[Scope, Throwable, RiskAssessment] =
    for
      observation <- eventInputPort.getEvents(subscribeTo patientBy identifier listenTo Event.Fall)
      patient     <- ZStream.fromZIO(pssRegistry.patient)
      chance      <- ZStream.fromZIO(fractureModel.compute(observation))
      riskAss     <- ZStream.succeed(makeRiskAssessment(chance))
    yield riskAss

  private def makeRiskAssessment(chance: Chance): RiskAssessment = ???

object FallDetectionService:
  val live: ZLayer[
    EventInputPort[Observation] & Registry & FractureModel[Observation],
    Nothing,
    FallDetectionService
  ] =
    ZLayer.fromFunction(FallDetectionService(_, _, _))

  def fallStreamByIdentifier(
      identifier: Identifier
  ): ZStream[Scope & FallDetectionService, Throwable, RiskAssessment] =
    ZStream.serviceWithStream[FallDetectionService](_.fallStreamByIdentifier(identifier))

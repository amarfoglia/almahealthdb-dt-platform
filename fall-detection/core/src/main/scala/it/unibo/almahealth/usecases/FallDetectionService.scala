package it.unibo.almahealth.usecases

import zio.stream.ZStream
import zio.ZIO
import zio.Scope
import it.unibo.almahealth.events.EventInputPort
import org.hl7.fhir.r4.model.Observation
import zio.ZLayer

class FallDetectionService(eventInputPort: EventInputPort[Observation]):
  def fallStream: ZStream[Scope, Throwable, Observation] =
    val subscription = EventInputPort.Topic("topic")
    eventInputPort.getEvents(subscription)

object FallDetectionService:

  val live: ZLayer[EventInputPort[Observation], Nothing, FallDetectionService] = ZLayer.fromFunction(FallDetectionService(_))

  def fallStream: ZStream[Scope & FallDetectionService, Throwable, Observation] = ZStream.serviceWithStream[FallDetectionService](_.fallStream)

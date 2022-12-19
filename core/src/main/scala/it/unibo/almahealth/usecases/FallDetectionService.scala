package it.unibo.almahealth.usecases

import zio.stream.ZStream
import zio.ZLayer
import org.hl7.fhir.r4.model.Observation
import zio._
import zio.kafka.consumer._
import zio.kafka.serde.Serde

import it.unibo.almahealth.events.EventInputPort

class FallDetectionService(eventInputPort: EventInputPort[Subscription, Observation]):
  def detectFalls: ZStream[Any, Throwable, Observation] =
    val subscription = Subscription.topics("topic")
    eventInputPort.getEvents(subscription)

object FallDetectionService:

  val live: ZLayer[EventInputPort[Subscription, Observation], Nothing, FallDetectionService] = ZLayer.fromFunction(FallDetectionService(_))

  def detectFalls: ZStream[FallDetectionService, Throwable, Observation] = ZStream.serviceWithStream[FallDetectionService](_.detectFalls)

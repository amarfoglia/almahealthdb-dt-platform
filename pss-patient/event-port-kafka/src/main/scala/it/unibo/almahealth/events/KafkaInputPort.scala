package it.unibo.almahealth.events

import zio.ZIO
import zio.ZLayer
import zio.stream.ZStream
import zio.kafka.consumer._
import zio.kafka.serde.Serde
import org.hl7.fhir.r4.model.Observation
import it.unibo.almahealth.context.ZFhirContext

type KafkaInputPort[+A] = EventInputPort[Subscription, A]

object KafkaInputPort:

  val live: ZLayer[ConsumerSettings & ZFhirContext, Throwable, KafkaInputPort[Observation]] =
    ZLayer.fromFunction { (settings: ConsumerSettings, ctx: ZFhirContext) =>
      val consumer = ZLayer.scoped { Consumer.make(settings) }
      new EventInputPort[Subscription, Observation]:
        override def getEvents(subscription: Subscription): ZStream[Any, Throwable, Observation] = (
          for
            parser <- ZStream.fromZIO(ctx.newJsonParser)
            observations <- (ZStream.fromZIO(Consumer.subscribe(subscription)) *> Consumer.plainStream(Serde.string, Serde.string))
              .flatMap { message =>
                ZStream.fromZIO(
                  parser.parseString(classOf[Observation], message.record.value)
                )
              }
          yield observations
        )
          .provideLayer(consumer)

    }

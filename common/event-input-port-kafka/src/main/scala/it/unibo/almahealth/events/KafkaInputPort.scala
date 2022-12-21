package it.unibo.almahealth.events

import it.unibo.almahealth.events.EventInputPort.Topic
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.kafka.consumer._
import zio.kafka.serde.Serde
import zio.stream.ZStream
import zio.Tag

case class KafkaInputPort(consumerSettings: ConsumerSettings) extends EventInputPort[String]:
  override def getEvents(topics: Set[Topic]): ZStream[Scope, Throwable, String] =
    val subscription = Subscription.Topics(topics.map(_.value))
    for
      consumer <- ZStream.fromZIO(Consumer.make(consumerSettings))
      _        <- ZStream.fromZIO(consumer.subscribe(subscription))
      read     <- consumer.plainStream(Serde.string, Serde.string)
    yield read.record.value

object KafkaInputPort:
  val live: ZLayer[ConsumerSettings, Nothing, KafkaInputPort] = ZLayer.fromFunction(KafkaInputPort(_))

  def deserialize[R: Tag, B: Tag](f: String => ZIO[R, Throwable, B]): ZLayer[EventInputPort[String] & R, Nothing, EventInputPort[B]] =
    ZLayer {
      for
        eeip <- ZIO.service[EventInputPort[String]]
        r <- ZIO.service[R]
      yield eeip.mapZIO(f.andThen(_.provide(ZLayer.succeed(r))))
    }

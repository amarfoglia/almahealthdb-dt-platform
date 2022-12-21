package it.unibo.almahealth.events

import zio.Scope
import zio.ZIO
import zio.stream.ZStream

import EventInputPort.*

object EventInputPort:
  case class Topic(value: String)

  private final case class MapZIO[A, B](base: EventInputPort[A], f: A => ZIO[Scope, Throwable, B]) extends EventInputPort[B]:
    override def getEvents(topics: Set[Topic]): ZStream[Scope, Throwable, B] =
      base.getEvents(topics).mapZIO(f)

trait EventInputPort[+A]:

  def getEvents(topics: Set[Topic]): ZStream[Scope, Throwable, A]

  def getEvents(topic: Topic): ZStream[Scope, Throwable, A] = getEvents(Set(topic))

  def getEvents(topic: Topic, rest: Topic*): ZStream[Scope, Throwable, A] = getEvents(Set(topic) ++ rest.toSet)

  def mapZIO[B](f: A => ZIO[Scope, Throwable, B]): EventInputPort[B] =
    MapZIO(this, f)

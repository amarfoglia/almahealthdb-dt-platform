package it.unibo.almahealth.events

import zio.stream.ZStream

trait EventInputPort[-B, +A]:
  def getEvents(b: B): ZStream[Any, Throwable, A]

package it.unibo.almahealth.presenter

import zio.ZIO

case class PresenterException(message: String) extends Throwable

trait Presenter[-B, +A]:
  def present(b: B): ZIO[Any, PresenterException, A]

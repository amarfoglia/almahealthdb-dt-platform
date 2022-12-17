package it.unibo.almahealth.domain

opaque type Identifier = String

object Identifier:
  def apply(in: String): Identifier = in

object syntax:
  object pattern:
    object Identifier:
      def unapply(in: String): Option[Identifier] = if in != ""
        then Some(it.unibo.almahealth.domain.Identifier(in))
        else None

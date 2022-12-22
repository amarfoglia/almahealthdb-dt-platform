package it.unibo.almahealth.context

import it.unibo.almahealth.domain.Identifier
import it.unibo.almahealth.events.EventInputPort.Topic

object Topics:

  private val patientsRoot: String = "patients"
  private val eventsRoot: String = "events"

  enum Event(val identifier: String):
    case Fall extends Event(???)
    case OtherEvent extends Event(???)
    case SomethingElse extends Event(???)

  final case class Patient (identifier: Identifier):
    def listenTo(event: Event): Topic = Topic(s"${patientsRoot}/${identifier.value}/${eventsRoot}/${event.identifier}")

  object syntax:
    final class SubscribeToBuilder:
      def patientBy(identifier: Identifier): Patient = Patient(identifier)

    def subscribeTo: SubscribeToBuilder = SubscribeToBuilder()

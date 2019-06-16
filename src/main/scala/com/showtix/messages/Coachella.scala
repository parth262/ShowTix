package com.showtix.messages

import akka.actor.Props
import akka.util.Timeout
import com.showtix.actors.Coachella

object Coachella {

    def props(implicit timeout: Timeout) = Props(new Coachella())

    case class CreateEvent(name: String, tickets: Int)
    case class GetEvent(name: String)
    case object GetEvents
    case class GetTickets(event: String, tickets: Int)
    case class CancelEvent(name: String)

    case class Event(name: String, tickets: Int)
    case class Events(events: Vector[Event])

    sealed trait EventResponse
    case class EventCreated(event: Event) extends EventResponse
    case object EventExists extends EventResponse
}

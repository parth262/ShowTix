package com.showtix.messages

import akka.actor.Props
import com.showtix.actors.TicketSeller

object TicketSeller {

    def props(event: String) = Props(new TicketSeller(event))

    case class Ticket(id: Int)
    case class Tickets(event: String, entries: Vector[Ticket] = Vector.empty[Ticket])
    case class Add(tickets: Vector[Ticket])
    case class Buy(tickets: Int)

    case object GetEvent
    case object Cancel
}

package com.showtix.actors

import akka.actor.{Actor, PoisonPill}
import com.showtix.messages.Coachella

class TicketSeller(event: String) extends Actor {
    import com.showtix.messages.TicketSeller._

    var tickets = Vector.empty[Ticket]

    def receive: PartialFunction[Any, Unit] = {
        case Add(newTickets) => tickets = tickets ++ newTickets

        case Buy(numberOfTickets) =>
            val entries = tickets.take(numberOfTickets)
            if(entries.size >= numberOfTickets) {
                sender() ! Tickets(event, entries)
                tickets = tickets.drop(numberOfTickets)
            } else sender() ! Tickets(event)

        case GetEvent => sender() ! Some(Coachella.Event(event, tickets.size))

        case Cancel => sender() ! Some(Coachella.Event(event, tickets.size))
        self ! PoisonPill
    }
}

package com.showtix.actors

import akka.actor.{Actor, ActorRef}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import com.showtix.messages.Coachella._

import scala.collection.immutable
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class Coachella(implicit timeout: Timeout) extends Actor {

    import com.showtix.messages.TicketSeller

    def createTicketSeller(name: String): ActorRef = {
        context.actorOf(TicketSeller.props(name), name)
    }

    def receive: PartialFunction[Any, Unit] = {
        case CreateEvent(name, tickets) =>
            def create(): Unit = {
                val eventTickets = createTicketSeller(name)
                val newTickets = (1 to tickets).map { ticketId => TicketSeller.Ticket(ticketId) }.toVector
                eventTickets ! TicketSeller.Add(newTickets)
                sender() ! EventCreated(Event(name, tickets))
            }

            context.child(name).fold(create())(_ => sender() ! EventExists)

        case GetTickets(event, tickets) =>
            def notFound(): Unit = sender() ! TicketSeller.Tickets(event)
            def buy(child: ActorRef): Unit = child forward TicketSeller.Buy(tickets)
            context.child(event).fold(notFound())(buy)

        case GetEvent(event) =>
            def notFound(): Unit = sender() ! None
            def getEvent(child: ActorRef): Unit = child forward TicketSeller.GetEvent
            context.child(event).fold(notFound())(getEvent)

        case GetEvents =>
            def getEvents: immutable.Iterable[Future[Option[Event]]] = context.children.map { child => {
                self.ask(GetEvent(child.path.name)).mapTo[Option[Event]]
            }}

            def convertToEvents(f: Future[Iterable[Option[Event]]]): Future[Events] = {
                f.map(_.flatten).map(l => Events(l.toVector))
            }
            pipe(convertToEvents(Future.sequence(getEvents))) to sender()

        case CancelEvent(event) =>
            def notFound(): Unit = sender() ! None
            def cancelEvent(child: ActorRef): Unit = child forward TicketSeller.Cancel
            context.child(event).fold(notFound())(cancelEvent)

    }
}

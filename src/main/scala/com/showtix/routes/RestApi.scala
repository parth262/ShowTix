package com.showtix.routes

import akka.actor.{ActorRef, ActorSystem}
import akka.util.Timeout
import com.showtix.messages.Coachella._
import com.showtix.messages._
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.model.StatusCodes
import StatusCodes._

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

class RestApi(system: ActorSystem, timeout: Timeout) extends RestRoutes {
  implicit val requestTimeout: Timeout = timeout

  implicit def executionContext: ExecutionContextExecutor = system.dispatcher

  def createCoachella(): ActorRef = system.actorOf(Coachella.props(timeout))
}

trait RestRoutes extends CoachellaApi with EventMarshaller {
  val service = "show-tix"
  val version = "v1"

  protected val createEventRoute: Route = {
    pathPrefix(service / version / "events" / Segment) { event =>
      post {
        pathEndOrSingleSlash {
          entity(as[EventDescription]) { ed =>
            onSuccess(createEvent(event, ed.tickets)) {
              case Coachella.EventCreated(evnt) => complete(Created, evnt)
              case Coachella.EventExists =>
                val err = Error(s"$event event already exists")
                complete(BadRequest, err)
            }
          }
        }
      }
    }
  }

  protected val getAllEventsRoute: Route = {
    pathPrefix(service / version / "events") {
      get {
        pathEndOrSingleSlash {
          onSuccess(getEvents) { events =>
            complete(OK, events)
          }
        }
      }
    }
  }

  protected val getEventRoute: Route = {
    pathPrefix(service / version / "events" / Segment) { event =>
      get {
        pathEndOrSingleSlash {
          onSuccess(getEvent(event)) {
            _.fold(complete(NotFound))(e => complete(OK, e))
          }
        }
      }
    }
  }

  protected val deleteEventRoute: Route = {
    pathPrefix(service / version / "events" / Segment) {event =>
      delete {
        pathEndOrSingleSlash {
          onSuccess(cancelEvent(event)) {
            _.fold(complete(NotFound))(e => complete(OK, e))
          }
        }
      }
    }
  }

  protected val purchaseEventTicketRoute: Route = {
    pathPrefix(service / version / "events" / Segment / "tickets") {event =>
      post {
        pathEndOrSingleSlash {
          entity(as[TicketRequests]) { request =>
            onSuccess(requestTickets(event, request.tickets)) {tickets =>
              if(tickets.entries.isEmpty) complete(NotFound)
              else complete(Created, tickets)
            }
          }
        }
      }
    }
  }

  val routes: Route = createEventRoute ~ getAllEventsRoute ~ getEventRoute ~ deleteEventRoute ~ purchaseEventTicketRoute
}

trait CoachellaApi {
  def createCoachella(): ActorRef

  implicit def executionContext: ExecutionContext

  implicit def requestTimeout: Timeout

  lazy val coachella: ActorRef = createCoachella()

  def createEvent(event: String, numberOfTickets: Int): Future[EventResponse] = {
    coachella.ask(CreateEvent(event, numberOfTickets)).mapTo[EventResponse]
  }

  def getEvents: Future[Events] = coachella.ask(GetEvents).mapTo[Events]

  def getEvent(event: String): Future[Option[Event]] = coachella.ask(GetEvent(event)).mapTo[Option[Event]]

  def cancelEvent(event: String): Future[Option[Event]] = coachella.ask(CancelEvent(event)).mapTo[Option[Event]]

  def requestTickets(event: String, tickets: Int): Future[TicketSeller.Tickets] = coachella.ask(GetTickets(event, tickets)).mapTo[TicketSeller.Tickets]

}

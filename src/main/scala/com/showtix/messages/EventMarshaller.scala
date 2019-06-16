package com.showtix.messages

import com.showtix.messages.Coachella._
import com.showtix.messages.TicketSeller.{Ticket, Tickets}
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import play.api.libs.json.{Json, OFormat}

case class EventDescription(tickets: Int) {
    require(tickets > 0)
}

case class TicketRequests(tickets: Int) {
    require(tickets > 0)
}

case class Error(message: String)

trait EventMarshaller extends PlayJsonSupport {
    implicit val eventDescriptionFormat: OFormat[EventDescription] = Json.format[EventDescription]
    implicit val ticketRequests: OFormat[TicketRequests] = Json.format[TicketRequests]
    implicit val errorFormat: OFormat[Error] = Json.format[Error]
    implicit val eventFormat: OFormat[Event] = Json.format[Event]
    implicit val eventsFormat: OFormat[Events] = Json.format[Events]
    implicit val ticketFormat: OFormat[Ticket] = Json.format[Ticket]
    implicit val ticketsFormat: OFormat[Tickets] = Json.format[Tickets]
}

object EventMarshaller extends EventMarshaller

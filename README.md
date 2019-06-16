# Show Tix

I created this application using a nice step by step demo and explanation by **Ian Shiundu** from [here](https://medium.com/@ian.shiundu/building-a-fully-functional-rest-service-using-akka-actors-8b5c12978380)

This application is build by purely using Akka libraries.

Its just a demo showing how a rest application can be build using akka

This is basically an Event Management Application.

It provides following features
 - Create an Event with name and number of tickets for the event.
    ```
    method: POST
    path: show-tix/v1/events/event_name
    body: {
        tickets: 200
    }
    ```
     
 - Get all the available events.
    ```
    method: GET
    path: show-tix/v1/events
    ```
    
 - Get particular event by its name.
    ```
    method: GET
    path: show-tix/v1/events/event_name
    ```
 - Purchase tickets for a particular event.
    ```
    method: POST
    path: show-tix/v1/events/event_name/tickets
    body {
        tickets: 30
    }
    ```
 - Delete an Event.
    ```
    method: DELETE
    path:show-tix/v1/events/event_name
    ```
    
To run the project simply download it and execute command `sbt run` from the project folder.
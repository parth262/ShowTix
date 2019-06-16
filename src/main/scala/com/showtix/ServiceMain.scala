package com.showtix

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.showtix.routes.RestApi
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.{ExecutionContextExecutor, Future}

object ServiceMain extends App with RequestTimeout {
    val config = ConfigFactory.load()
    val host = config.getString("http.host")
    val port = config.getInt("http.port")

    implicit val system: ActorSystem = ActorSystem()
    implicit val ec: ExecutionContextExecutor = system.dispatcher

    val api = new RestApi(system, requestTimeout(config)).routes

    implicit val materializer: ActorMaterializer = ActorMaterializer()

    val bindingFuture: Future[ServerBinding] = Http().bindAndHandle(api, host, port)

    val log = Logging(system.eventStream, "show-tix")

    try {
        bindingFuture map {serverBinding =>
          log.info(s"RestApi bound to ${serverBinding.localAddress}")
        }
    } catch {
        case ex: Exception =>
            log.error(ex, s"Failed to bind to $host:$port")
        system.terminate()
    }

}

trait RequestTimeout {
    import scala.concurrent.duration._
    def requestTimeout(config: Config): Timeout = {
        val t = config.getString("akka.http.server.request-timeout")
        val d = Duration(t)
        FiniteDuration(d.length, d.unit)
    }
}

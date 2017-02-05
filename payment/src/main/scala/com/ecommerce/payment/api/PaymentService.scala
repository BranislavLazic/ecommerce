package com.ecommerce.payment.api

import akka.actor.ActorSystem
import akka.util.Timeout
import akka.http.scaladsl.server.{Route, Directives}
import akka.http.scaladsl.model.StatusCodes
import de.heikoseeberger.akkahttpcirce.CirceSupport

import scala.concurrent.ExecutionContext

/**
  * Created by lukewyman on 2/5/17.
  */
class PaymentService(val system: ActorSystem, val requestTimeout: Timeout) extends PaymentRoutes {
  val executionContext = system.dispatcher
}

trait PaymentRoutes {
  import Directives._
  import StatusCodes._
  import CirceSupport._
  import io.circe.generic.auto._

  implicit def executionContext: ExecutionContext
  implicit def requestTimeout: Timeout

  def routes: Route = pay

  def pay: Route = {
    post {
      pathPrefix("payments") {
        pathEndOrSingleSlash {
          complete(OK)
        }
      }
    }
  }
}

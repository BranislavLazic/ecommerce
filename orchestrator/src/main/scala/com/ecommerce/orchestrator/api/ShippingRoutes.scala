package com.ecommerce.orchestrator.api

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Route, Directives}
import akka.util.Timeout
import de.heikoseeberger.akkahttpcirce.CirceSupport

import scala.concurrent.ExecutionContext

/**
  * Created by lukewyman on 1/31/17.
  */
trait ShippingRoutes {

  import Directives._
  import StatusCodes._
  import CirceSupport._
  import io.circe.generic.auto._

  def system: ActorSystem

  implicit def requestTimeout: Timeout
  implicit def executionContext: ExecutionContext

  def shippingRoutes: Route = ???

}

package com.ecommerce.orchestrator.backend.clientapi

import akka.actor.{Actor, ActorRef}
import akka.util.Timeout
import com.ecommerce.common.clientactors.http.{PaymentHttpClient, HttpClient}
import com.ecommerce.common.clientactors.http.PaymentHttpClient.Pay
import com.ecommerce.common.views.PaymentResponse.PaymentTokenView

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by lukewyman on 2/8/17.
  */
trait PaymentApi { this: Actor =>
  import HttpClient._
  import akka.pattern.ask

  implicit def executionContext: ExecutionContext
  implicit def timeout: Timeout

  def paymentClient = context.actorOf(PaymentHttpClient.props)

  def pay(creditCard: String): Future[HttpClientResult[PaymentTokenView]] =
    paymentClient.ask(Pay(creditCard)).mapTo[HttpClientResult[PaymentTokenView]]
}

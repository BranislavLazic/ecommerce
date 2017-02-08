package com.ecommerce.orchestrator.backend.actor.orchestrator

import akka.actor.ActorRef
import akka.util.Timeout
import com.ecommerce.common.clientactors.http.HttpClient
import com.ecommerce.common.clientactors.http.PaymentHttpClient.Pay
import com.ecommerce.common.views.PaymentResponse.PaymentTokenView

import scala.concurrent.{Future, ExecutionContext}

/**
  * Created by lukewyman on 2/8/17.
  */
trait PaymentApi {
  import akka.pattern.ask
  import HttpClient._

  implicit def executionContext: ExecutionContext
  implicit def timeout: Timeout

  def paymentClient: ActorRef

  def pay(creditCard: String): Future[HttpClientResult[PaymentTokenView]] =
    paymentClient.ask(Pay(creditCard)).mapTo[HttpClientResult[PaymentTokenView]]
}

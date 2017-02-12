package com.ecommerce.orchestrator.backend.orchestrator

import java.time.ZonedDateTime
import java.util.UUID

import akka.actor.Status.Success
import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.testkit.{TestProbe, DefaultTimeout, ImplicitSender, TestKit}
import com.ecommerce.common.views.InventoryResponse.InventoryItemView
import com.ecommerce.common.views.ReceivingResponse
import com.ecommerce.orchestrator.backend.ResponseViews
import org.scalatest.{WordSpecLike, MustMatchers}
import com.ecommerce.common.clientactors.protocols.ReceivingProtocol
import com.ecommerce.common.clientactors.protocols.InventoryProtocol
import scala.concurrent.duration._
import scala.util.{Success, Failure}

/**
  * Created by lukewyman on 2/11/17.
  */
class ReceivingOrchestratorSpec extends TestKit(ActorSystem("test-receiving-orchestrator"))
with WordSpecLike
with MustMatchers
with ImplicitSender
with DefaultTimeout
with StopSystemAfterAll {

  import akka.pattern.ask
  import ReceivingOrchestrator._
  import ReceivingProtocol._
  import InventoryProtocol._
  import ReceivingResponse._
  import ResponseViews._

  "The ReceivingOrchestrator" must {

    "Return a ReceivingSummaryView for GetShipment" in {

      val receivingClientProbe = TestProbe("receiving-client")
      val inventoryClientProbe = TestProbe("inventory-client")
      val inventoryQueueProbe = TestProbe("inventory-queue")

      val receivingOrchestrator = system.actorOf(Props(
        new ReceivingOrchestrator {
          override val receivingClient = receivingClientProbe.ref
          override val inventoryClient = inventoryClientProbe.ref
          override val inventoryQueue = inventoryQueueProbe.ref
        }
      ), "receiving-orchestrator")

      val shipmentId = UUID.randomUUID()
      val productId = UUID.randomUUID()
      val ordered = ZonedDateTime.now.plusDays(10)
      val expectedDelivery = null.asInstanceOf[ZonedDateTime]
      val delivered = null.asInstanceOf[ZonedDateTime]
      val count = 100

      val result = receivingOrchestrator ! GetShipmentSummary(shipmentId)
      receivingClientProbe.expectMsg(GetShipment(shipmentId))
      receivingClientProbe.reply(Right(ShipmentView(shipmentId, productId,  ordered, expectedDelivery, delivered, count)))
      inventoryClientProbe.expectMsg(GetItem(productId))
      inventoryClientProbe.reply(Right(InventoryItemView(productId, 20, 10)))
      inventoryQueueProbe.expectNoMsg()


      expectMsg(5 seconds, Right(ReceivingSummaryView(
        productId,
        shipmentId,
        ordered,
        count,
        expectedDelivery,
        delivered,
        20,
        10
      )))
    }
  }
}

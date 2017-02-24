package com.ecommerce.orchestrator.backend.orchestrator

import java.time.ZonedDateTime
import java.util.UUID

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.testkit.{TestProbe, DefaultTimeout, ImplicitSender, TestKit}
import com.ecommerce.common.identity.Identity
import com.ecommerce.common.views.InventoryResponse
import com.ecommerce.common.views.ReceivingResponse
import com.ecommerce.orchestrator.backend.ResponseViews
import org.scalatest.{WordSpecLike, MustMatchers}
import com.ecommerce.common.clientactors.protocols.ReceivingProtocol
import com.ecommerce.common.clientactors.protocols.InventoryProtocol
import scala.concurrent.duration._

/**
  * Created by lukewyman on 2/11/17.
  */
class ReceivingOrchestratorSpec extends TestKit(ActorSystem("test-receiving-orchestrator"))
  with WordSpecLike
  with MustMatchers
  with ImplicitSender
  with DefaultTimeout
  with StopSystemAfterAll {

  import ReceivingOrchestrator._
  import ReceivingProtocol._
  import InventoryProtocol._
  import ReceivingResponse._
  import InventoryResponse._
  import ResponseViews._
  import Identity._

  "The ReceivingOrchestrator" must {

    "Return a ReceivingSummaryView for GetShipment" in {

      val (receivingOrchestrator, receivingClientProbe, inventoryClientProbe, inventoryQueueProbe) =
        createTestActors("get-shipment")

      val shipmentId = UUID.randomUUID()
      val productId = UUID.randomUUID()
      val ordered = ZonedDateTime.now
      val expectedDelivery = null.asInstanceOf[ZonedDateTime]
      val delivered = null.asInstanceOf[ZonedDateTime]
      val count = 100

      receivingOrchestrator ! GetShipmentSummary(ShipmentRef(shipmentId))
      receivingClientProbe.expectMsg(GetShipment(ShipmentRef(shipmentId)))
      receivingClientProbe.reply(Right(ShipmentView(shipmentId, productId,  ordered, expectedDelivery, delivered, count)))
      inventoryClientProbe.expectMsg(GetItem(ProductRef(productId)))
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

   "Return a ReceivingSummaryView for RequestShipment" in {
     val (receivingOrchestrator, receivingClientProbe, inventoryClientProbe, inventoryQueueProbe) =
       createTestActors("request-shipment")

     val shipmentId = UUID.randomUUID()
     val productId = UUID.randomUUID()
     val count = 100
     val ordered = ZonedDateTime.now
     val expectedDelivery = null.asInstanceOf[ZonedDateTime]
     val delivered = null.asInstanceOf[ZonedDateTime]

     receivingOrchestrator ! RequestShipment(ProductRef(productId), ordered, count)
     receivingClientProbe.expectMsg(CreateShipment(ProductRef(productId), ordered, count))
     receivingClientProbe.reply(Right(ShipmentView(shipmentId, productId, ordered, expectedDelivery, delivered, count)))
     inventoryClientProbe.expectMsg(GetItem(ProductRef(productId)))
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

  "Return a ReceivingSummaryView for AcknowledgeShipment" in {
    val (receivingOrchestrator, receivingClientProbe, inventoryClientProbe, inventoryQueueProbe) =
      createTestActors("acknowledge-shipment")

    val shipmentId = UUID.randomUUID()
    val productId = UUID.randomUUID()
    val count = 100
    val ordered = ZonedDateTime.now.plusDays(-10)
    val expectedDelivery = ZonedDateTime.now
    val delivered = null.asInstanceOf[ZonedDateTime]

    receivingOrchestrator ! ReceivingOrchestrator.AcknowledgeShipment(ProductRef(productId), ShipmentRef(shipmentId), expectedDelivery, count)
    receivingClientProbe.expectMsg(ReceivingProtocol.AcknowledgeShipment(ShipmentRef(shipmentId), expectedDelivery))
    receivingClientProbe.reply(Right(ShipmentView(shipmentId, productId, ordered, expectedDelivery, delivered, count)))
    inventoryClientProbe.expectMsg(NotifySupply(ProductRef(productId), ShipmentRef(shipmentId), expectedDelivery, count))
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

  "Return a ReceivingSummaryView for AcceptShipment" in {
    val (receivingOrchestrator, receivingClientProbe, inventoryClientProbe, inventoryQueueProbe) =
      createTestActors("accept-shipment")

    val shipmentId = UUID.randomUUID()
    val productId = UUID.randomUUID()
    val count = 100
    val ordered = ZonedDateTime.now.plusDays(-20)
    val expectedDelivery = ZonedDateTime.now.plusDays(-10)
    val delivered = ZonedDateTime.now

    receivingOrchestrator ! ReceivingOrchestrator.AcceptShipment(ProductRef(productId), ShipmentRef(shipmentId), delivered, count)
    receivingClientProbe.expectMsg(ReceivingProtocol.AcceptShipment(ShipmentRef(shipmentId)))
    receivingClientProbe.reply(Right(ShipmentView(shipmentId, productId, ordered, expectedDelivery, delivered, count)))
    inventoryClientProbe.expectMsg(ReceiveSupply(ProductRef(productId), ShipmentRef(shipmentId), delivered, count))
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

  def createTestActors(orcchestratorName: String): (ActorRef, TestProbe, TestProbe, TestProbe) = {
    val receivingClientProbe = TestProbe("receiving-client")
    val inventoryClientProbe = TestProbe("inventory-client")
    val inventoryQueueProbe = TestProbe("inventory-queue")

    (system.actorOf(Props(
      new ReceivingOrchestrator {
        override val receivingClient = receivingClientProbe.ref
        override val inventoryClient = inventoryClientProbe.ref
        override val inventoryQueue = inventoryQueueProbe.ref
      }
    ), orcchestratorName),
      receivingClientProbe, inventoryClientProbe, inventoryQueueProbe)
  }

}

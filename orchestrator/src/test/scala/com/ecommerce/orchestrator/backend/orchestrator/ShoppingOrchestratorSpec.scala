package com.ecommerce.orchestrator.backend.orchestrator

import java.time.ZonedDateTime
import java.util.UUID

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.testkit.{TestProbe, DefaultTimeout, ImplicitSender, TestKit}
import com.ecommerce.common.views.InventoryResponse
import com.ecommerce.common.views.ShoppingCartResponse
import com.ecommerce.orchestrator.backend.ResponseViews
import com.ecommerce.orchestrator.backend.orchestrator.ShoppingOrchestrator
import org.scalatest.{WordSpecLike, MustMatchers}
import com.ecommerce.common.clientactors.protocols.ShoppingCartProtocol
import com.ecommerce.common.clientactors.protocols.InventoryProtocol
import com.ecommerce.common.clientactors
import scala.concurrent.duration._

/**
  * Created by lukewyman on 2/11/17.
  */
class ShoppingOrchestratorSpec extends TestKit(ActorSystem("test-shopping-orchestrator"))
  with WordSpecLike
  with MustMatchers
  with ImplicitSender
  with DefaultTimeout
  with StopSystemAfterAll{

  import ShoppingOrchestrator._
  import InventoryProtocol._
  import InventoryResponse._
  import ShoppingCartProtocol._
  import ShoppingCartResponse._
  import ResponseViews._

  "The ShoppingOrchestrator" must {

    "Return a ShoppingCartView for StartShopping" in {

      val inventoryClientProbe = TestProbe("inventory-client")
      val inventoryQueueProbe = TestProbe("inventory-queue")
      val paymentClientProbe = TestProbe("payment-client")
      val shoppingCartClientProbe = TestProbe("shoppingcart-client")

      val shoppingOrchestrator = system.actorOf(Props(
        new ShoppingOrchestrator {
          override val inventoryClient = inventoryClientProbe.ref
          override val inventoryQueue = inventoryQueueProbe.ref
          override val paymentClient = paymentClientProbe.ref
          override val shoppingCartClient = shoppingCartClientProbe.ref
        }
      ), "shopping-orchestrator")

      val shoppingCartId = UUID.randomUUID()
      val customerId = UUID.randomUUID()

      shoppingOrchestrator ! StartShopping(shoppingCartId, customerId)
      shoppingCartClientProbe.expectMsg(CreateShoppingCart(shoppingCartId, customerId))
      shoppingCartClientProbe.reply(Right(ShoppingCartView(shoppingCartId, Some(customerId), List.empty)))
      expectMsg(Right(ShoppingCartView(shoppingCartId, Some(customerId), List.empty)))
    }
  }
}

package com.ecommerce.orchestrator.backend.orchestrator

import java.time.ZonedDateTime
import java.util.UUID

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.testkit.{TestProbe, DefaultTimeout, ImplicitSender, TestKit}
import com.ecommerce.common.clientactors.http.PaymentHttpClient.Pay
import com.ecommerce.common.views.InventoryRequest.HoldItemView
import com.ecommerce.common.views.InventoryResponse
import com.ecommerce.common.views.PaymentResponse.PaymentTokenView
import com.ecommerce.common.views.ShoppingCartResponse
import com.ecommerce.orchestrator.backend.ResponseViews
import org.scalatest.{WordSpecLike, MustMatchers}
import com.ecommerce.common.clientactors.protocols.ShoppingCartProtocol
import com.ecommerce.common.clientactors.protocols.InventoryProtocol

/**
  * Created by lukewyman on 2/11/17.
  */
class ShoppingOrchestratorSpec extends TestKit(ActorSystem("test-shopping-orchestrator"))
  with WordSpecLike
  with MustMatchers
  with ImplicitSender
  with DefaultTimeout
  with StopSystemAfterAll {

  import ShoppingOrchestrator._
  import InventoryProtocol._
  import InventoryResponse._
  import ShoppingCartProtocol._
  import ShoppingCartResponse._
  import ResponseViews._

  "The ShoppingOrchestrator" must {

    "Return a ShoppingCartView for StartShopping" in {

      val (shoppingOrchestrator, inventoryClientProbe, inventoryQueueProbe, paymentClientProbe,
        shoppingCartClientProbe) = createTestActors("start-shopping")

      val shoppingCartId = UUID.randomUUID()
      val customerId = UUID.randomUUID()

      shoppingOrchestrator ! StartShopping(shoppingCartId, customerId)
      shoppingCartClientProbe.expectMsg(CreateShoppingCart(shoppingCartId, customerId))
      shoppingCartClientProbe.reply(Right(ShoppingCartView(shoppingCartId, Some(customerId), List.empty)))
      inventoryClientProbe.expectNoMsg()
      inventoryQueueProbe.expectNoMsg()
      paymentClientProbe.expectNoMsg()
      expectMsg(Right(ShoppingCartView(shoppingCartId, Some(customerId), List.empty)))
    }

    "Return a ShoppingCartView for PlaceInCart" in {

      val (shoppingOrchestrator, inventoryClientProbe, inventoryQueueProbe, paymentClientProbe,
      shoppingCartClientProbe) = createTestActors("place-in-cart")

      val shoppingCartId = UUID.randomUUID()
      val customerId = UUID.randomUUID()
      val productId = UUID.randomUUID()
      val count = 2
      val takeFromStock = false

      shoppingOrchestrator ! PlaceInCart(shoppingCartId, productId, count, takeFromStock)
      inventoryClientProbe.expectMsg(HoldItem(shoppingCartId, productId, count))
      inventoryClientProbe.reply(Right(HoldItemView(count)))
      shoppingCartClientProbe.expectMsg(AddItem(shoppingCartId, productId, count))
      shoppingCartClientProbe.reply(Right(ShoppingCartView(shoppingCartId, Some(customerId),
        List(ShoppingCartItemView(productId, count)))))
      inventoryQueueProbe.expectNoMsg()
      paymentClientProbe.expectNoMsg()
      expectMsg(Right(ShoppingCartView(shoppingCartId, Some(customerId),
        List(ShoppingCartItemView(productId, count)))))
    }

    "Return a ShoppingCartView for RemoveFromCart" in {

      val (shoppingOrchestrator, inventoryClientProbe, inventoryQueueProbe, paymentClientProbe,
      shoppingCartClientProbe) = createTestActors("remove-from-cart")

      val shoppingCartId = UUID.randomUUID()
      val customerId = UUID.randomUUID()
      val productId = UUID.randomUUID()
      val count = 2
      val takeFromStock = false

      shoppingOrchestrator ! RemoveFromCart(shoppingCartId, productId)
      shoppingCartClientProbe.expectMsg(RemoveItem(shoppingCartId, productId))
      shoppingCartClientProbe.reply(Right(ShoppingCartView(shoppingCartId, Some(customerId), List.empty)))
      inventoryQueueProbe.expectMsg(ReleaseItem(productId, shoppingCartId))
      inventoryClientProbe.expectNoMsg()
      paymentClientProbe.expectNoMsg()
      expectMsg(Right(ShoppingCartView(shoppingCartId, Some(customerId), List.empty)))
    }

    "Return a ShoppingCartView for AbandonCart" in {

      val (shoppingOrchestrator, inventoryClientProbe, inventoryQueueProbe, paymentClientProbe,
      shoppingCartClientProbe) = createTestActors("abandon-cart")

      val shoppingCartId = UUID.randomUUID()
      val customerId = UUID.randomUUID()
      val productId1 = UUID.randomUUID()
      val productId2 = UUID.randomUUID()
      val count1 = 2
      val count2 = 1
      val takeFromStock = false

      shoppingOrchestrator ! AbandonCart(shoppingCartId)
      shoppingCartClientProbe.expectMsg(GetShoppingCart(shoppingCartId))
      shoppingCartClientProbe.reply(Right(ShoppingCartView(shoppingCartId, Some(customerId),
        List(ShoppingCartItemView(productId1, count1), ShoppingCartItemView(productId2, count2)))))
      inventoryQueueProbe.expectMsg(ReleaseItem(productId1, shoppingCartId))
      inventoryQueueProbe.expectMsg(ReleaseItem(productId2, shoppingCartId))
      shoppingCartClientProbe.expectMsg(ClearCart(shoppingCartId))
      shoppingCartClientProbe.reply(Right(ShoppingCartView(shoppingCartId, Some(customerId), List.empty)))
      inventoryClientProbe.expectNoMsg()
      paymentClientProbe.expectNoMsg()
      expectMsg(Right(ShoppingCartView(shoppingCartId, Some(customerId), List.empty)))
    }

    "Return a ShoppingCartView for Checkout" in {

      val (shoppingOrchestrator, inventoryClientProbe, inventoryQueueProbe, paymentClientProbe,
      shoppingCartClientProbe) = createTestActors("checkout")

      val shoppingCartId = UUID.randomUUID()
      val customerId = UUID.randomUUID()
      val productId1 = UUID.randomUUID()
      val productId2 = UUID.randomUUID()
      val count1 = 2
      val count2 = 1
      val takeFromStock = false
      val paymentId = UUID.randomUUID()

      shoppingOrchestrator ! Checkout(shoppingCartId, "credit-card-number")
      shoppingCartClientProbe.expectMsg(GetShoppingCart(shoppingCartId))
      shoppingCartClientProbe.reply(Right(ShoppingCartView(shoppingCartId, Some(customerId),
        List(ShoppingCartItemView(productId1, count1), ShoppingCartItemView(productId2, count2)))))
      paymentClientProbe.expectMsg(Pay("credit-card-number"))
      paymentClientProbe.reply(Right(PaymentTokenView(paymentId)))
      inventoryQueueProbe.expectMsg(ClaimItem(shoppingCartId, productId1, paymentId))
      inventoryQueueProbe.expectMsg(ClaimItem(shoppingCartId, productId2, paymentId))
      inventoryClientProbe.expectNoMsg()
      expectMsg(Right(ShoppingCartView(shoppingCartId, Some(customerId),
        List(ShoppingCartItemView(productId1, count1), ShoppingCartItemView(productId2, count2)))))
    }
  }

  def createTestActors(orchestratorName: String): (ActorRef, TestProbe, TestProbe, TestProbe, TestProbe) = {
    val inventoryClientProbe = TestProbe("inventory-client")
    val inventoryQueueProbe = TestProbe("inventory-queue")
    val paymentClientProbe = TestProbe("payment-client")
    val shoppingCartClientProbe = TestProbe("shoppingcart-client")

    (system.actorOf(Props(
      new ShoppingOrchestrator {
        override val inventoryClient = inventoryClientProbe.ref
        override val inventoryQueue = inventoryQueueProbe.ref
        override val paymentClient = paymentClientProbe.ref
        override val shoppingCartClient = shoppingCartClientProbe.ref
      }
    ), orchestratorName),
      inventoryClientProbe, inventoryQueueProbe, paymentClientProbe, shoppingCartClientProbe)
  }
}

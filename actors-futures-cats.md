### Combining web service calls with Actors and Futures 

Akka actors and Scala concurrency are effective tools for creating concurrent applications. Scala Futures represent concurrent functions, and provide a way to maninpuate and combine concurrent tasks without blocking. Akka actors are essentially lightweight concurrent objects, multiple tasks to be carried out concurrently. Combining the two...

This post describes how the [Orchestrator](https://github.com/lukewyman/ecommerce/tree/master/orchestrator) in ecommerce uses Actors and Futures to coordinate calls to microservices for a given use case. 

The Orchestrator is the heart of e-commerce. Its REST API is the to which all shopping and store management requests are made. Each use case will usually consist of a series of web service calls to the applicable microservices. For example, the use case of an inventory clerk viewing the status of an order to a wholesaler for a given Product would entail:

+ a GET call to the Receiving microservice to get the Shipment
+ a GET call to the Inventory microservice to get a summary of state of the Product in Inventory
+ a GET call to the Product microservice to get the product details for display purposes
+ some crunching to create a unified response view

#### Making the calls





```scala

class ReceivingOrchestrator(timeout: Timeout) extends Actor {
  import akka.pattern.ask
  import akka.pattern.pipe
  // other imports...

  implicit def executionContext = context.dispatcher // the ExecutionContext to run the Futures

  // the ActorRefs of the HttpClient actors
  def inventoryClient = context.actorOf(InventoryHttpClient.props)
  def productClient = context.actorOf(ProductHttpClient.props)
  val receivingClient = context.actorOf(ReceivingHttpClient.props)

  def receive = {
    case GetShipmentSummary(sid) =>
      
      // A Monadic flow to combine the results of the returned messages.
      val result = for {
        gs <- receivingClient.ask(GetShipment(sid)).mapTo[ShipmentView]  // each call returns a Future with a response
        gi <- inventoryClient.ask(GetItem(ProductRef(gs.productId))).mapTo[InventoryItemView]
        gp <- productClient.ask(GetProductByProductId(ProductRef(gs.productId))).mapTo[ProductView]
      } yield mapToReceivingSummaryView(gs, gi, gp)
      
      result.pipeTo(sender()) // complete the Future and send the results to the sender
  }
}

```

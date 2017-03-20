## Combining web service calls with Actors and Futures 

Akka actors and Scala concurrency are effective tools for creating concurrent applications. Scala Futures represent concurrent functions, and provide a way to maninpuate and combine concurrent tasks without blocking (or, as we'll see later, a way to isolate and manage blocking when working with Futures). Akka actors are essentially lightweight concurrent objects, representing multiple tasks to be carried out concurrently. Combining the two yields a powerful tool set for simplifying managing concurrent tasks. 

This post describes how the [Orchestrator](https://github.com/lukewyman/ecommerce/tree/master/orchestrator) in ecommerce uses Actors and Futures to coordinate calls to microservices. The microservice APIs in ecommerce are exposed as REST endpoints, so each microservice is represented by an HTTP Client actor in the Orchestrator module. For insights on how to build an Akka actor that works as an HTTP client using Akka HTTP, take a look at [Creating an HTTP Client Actor](http-client-actor.md). 

The Orchestrator module is the heart of e-commerce. Its REST API is the top-level interface at which all shopping and store management requests are made. Each use case consists of a series of web service calls to the applicable microservices, which must then be combined in some way to return a meaningful response to the user. For example, the use case of an inventory clerk viewing the status of an order to a wholesaler for a given Product would entail:

+ a GET call to the Receiving microservice to get the Shipment
+ a GET call to the Inventory microservice to get a summary of state of the Product in Inventory
+ a GET call to the Product microservice to get the product details for display purposes
+ some crunching to create a unified response view

A good way to structure a solution to a problem like this, is to create a coordinating actor for each group of related use cases. The `ReceivingActor`, which I will use as the example for this post, will handle the use cases relevant to the Receiving department of ecommerce....

A caveat to all this, is that composing such a solutuon when working on a non-trivial project requires managing complexity. Calls to web services aren't always successful, so error handling is required. Managing concurrency in a production-bound project isn't always straigh forward. And, finally, the code required to deal with all this can get a little unwieldy for one little actor class. It's going to take a couple of passes to get this done.

### Making the calls



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

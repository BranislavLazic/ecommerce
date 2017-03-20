## Combining web service calls with Actors and Futures 

Akka actors and Scala concurrency are effective tools for creating concurrent applications. Scala Futures represent concurrent functions, and provide a way to maninpuate and combine concurrent tasks without blocking (or, as we'll see later, provide a way to isolate and manage blocking when working with Futures). Akka actors are essentially lightweight concurrent objects, representing multiple tasks to be carried out concurrently. Combining the two yields a powerful tool set for simplifying managing concurrent tasks. 

This post describes how the [Orchestrator](https://github.com/lukewyman/ecommerce/tree/master/orchestrator) in ecommerce uses Actors and Futures to coordinate calls to microservices. The microservice APIs in ecommerce are exposed as REST endpoints, so each microservice is represented by an HTTP Client actor in the Orchestrator module. For insights on how to build an Akka actor that works as an HTTP client using Akka HTTP, take a look at [Creating an HTTP Client Actor](http-client-actor.md). 

The Orchestrator module is the heart of e-commerce. Its REST API is the top-level interface at which all shopping and store management requests are made. Each use case consists of a series of web service calls to the applicable microservices, the responses of which must then be combined in some way to return a meaningful response to the user. For example, the use case of an inventory clerk viewing the status of an order to a wholesaler for a given Product would entail:

+ a GET call to the Receiving microservice to get the Shipment
+ a GET call to the Inventory microservice to get a summary of the state of the Product in Inventory
+ a GET call to the Product microservice to get the Product details for display purposes
+ some crunching to create a unified response view

A good way to structure a solution to a problem like this, is to create a coordinating actor for each group of related use cases. The coordinating actor would then have an HTTP Client actor for each microservice that it needs to call. The HTTP Client actors are thus children of the coordinating actor. The coordinating actor will make calls to each of the child actors necessary for a given request, and then combine the resulting Futures to create the response to be returned to the Orchestrator REST API.

A caveat to all this, is that composing such a solutuon when working on a non-trivial project requires managing complexity. Calls to web services aren't always successful, so error handling is required. Managing concurrency in a production-bound project isn't always straigh forward. And, finally, the code required to deal with all this can get a little unwieldy for one little actor class. It's going to take a couple of passes to get this done.

### The basic structure

The `ReceivingActor`, which we will use as the example for this exercise, will handle the use cases relevant to the Receiving department of ecommerce. The `ReceivingActor` requires three HTTP Client actors as children - an `InventoryHttpClient` for the Inventory microservice, a `ProductHttpClient` for Product, and a `ReceivingHttpClient` for Receiving. So a start to the `ReceivingOrchestrator` might look something like this:

```scala
class ReceivingOrchestrator extends Actor {

  def inventoryClient = context.actorOf(InventoryHttpClient.props)
  def productClient = context.actorOf(ProductHttpClient.props)
  val receivingClient = context.actorOf(ReceivingHttpClient.props)
  
  def receive = ???
}
```

### Making the calls

Working with Futures in actors is pretty straight forward. The `ReceivingOrchestrator` actor asks an Http Client actor for a REST response by sending it a message with the `ask` pattern. Using `ask` always returns a Future of the result:
 
 ```scala
 val gs: Future[ShipmentView] = receivingClient.ask(GetShipment(sid)).mapTo[ShipmentView]
 ```
 
 `gs` is a Future[ShipmentView], which takes as long to complete as it takes for the `ReceivingHttpClient` actor to send a reply. Since futures form Monads (i.e. they have a method called `flatMap`), we can manipulate and combine several Futures in a 'Monadic Flow', which results in a Future containing our final result. In this case, we want to `ask` the `ReceivingHttpClient`, the `InventoryHttpClient` and the `ProductHttpClient` for the responses we'll need to create a combined response that displays the status of the Shipment, along with the status of the Product in Inventory, and the Product details. `mapToReceivingSummaryView` is a function that takes the responses from each of the Http Client actors (a `ShipmentView`, an `InventoryItemView` and a `ProductView`)  and combines them to create a `ReceivingSummaryView`, the response that will be returned to the sender.
 
 Finally, Akka provides a `pipe` pattern to send the result of the `Future[ReceivingSummaryView]` to the sender, in this case, the Akka HTTP REST API. It is important to note that this is the point at which the Future blocks. The `pipe` pattern waits for the Future to complete, before sending the result, a naked `ReceivingSummaryView` to the sender. Putting this all together, gives us:
  

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
        gs <- receivingClient.ask(GetShipment(sid)).mapTo[ShipmentView]  // each call returns a Future with an HTTP Client response
        gi <- inventoryClient.ask(GetItem(ProductRef(gs.productId))).mapTo[InventoryItemView]
        gp <- productClient.ask(GetProductByProductId(ProductRef(gs.productId))).mapTo[ProductView]
      } yield mapToReceivingSummaryView(gs, gi, gp)
      
      result.pipeTo(sender()) // complete the Future and send the results to the sender
  }
}

```

Not bad, so far. But, if we're going to be doing a lot of this, all those HTTP Client calls are going to become tedious and repetitive. The HTTP Client actors are going to be reused in other groupings of use cases. The `ShoppingOrchestrator`, for example, will also use the `ProductHttpClient` and the `InventoryHttpClient` actors. The code to prepare the messages and do the asking gets a cumbersome and in the way. It would be nice to abstract this away into a set of reusable client API traits.

### The client APIs

A clean design that would achieve this, would be to separate the `ask` calls into a series of traits - one for each HTTP Client actor. So, where before getting a `Future[SomeResponseView]` would be done with the `ask` pattern and a child actor, the `ReceivingOrchestrator` would simply mix in the client trait and call a method like `getShipment(shipmentId)`. The HTTP Client actor would also be instantiated in the trait. Here is the `ReceivingApi` trait as an example:

```scala

trait ReceivingApi { this: Actor =>  
  import akka.pattern.ask
  // other imports...

  implicit def executionContext: ExecutionContext
  implicit def timeout: Timeout

  val receivingClient = context.actorOf(ReceivingHttpClient.props)

  def getShipment(shipmentId: ShipmentRef): Future[HttpClientResult[ShipmentView]] =
    receivingClient.ask(GetShipment(shipmentId)).mapTo[HttpClientResult[ShipmentView]]

  def createShipment(productId: ProductRef, ordered: ZonedDateTime, count: Int): Future[HttpClientResult[ShipmentView]] =
    receivingClient.ask(CreateShipment(productId, ordered, count)).mapTo[HttpClientResult[ShipmentView]]

  // Other calls to receivingClient
}

```

Similarly, a trait for each of the `ProductHttpClient` and `InventoryHttpClient` actors need to be created. All three traits are mixed into the `ReceivingOrchestrator` actor. The result is a much cleaner coordinate actor:

```
class ReceivingOrchestrator extends Actor 
  import akka.pattern.pipe
  // other imports...
  
  def receive = {
    case GetShipmentSummary(sid) =>      
      val result = for {
        gs <- getShipment(sid)  // each call to an HTTP Client is replaced with a method call to the API trait
        gi <- getInventoryItem(ProductRef(gs.productId))
        gp <- getProductByProductId(gs.productId)
      } yield mapToReceivingSummaryView(gs, gi, gp)

      result.pipeTo(sender()) // complete the Future and send the results to the sender
  }
```

### Handling errors from the HTTP clients



#### The EitherT Monad Transformer from Cats


#### Other things Cats can do


### Creating and configuring a blocking dispatcher




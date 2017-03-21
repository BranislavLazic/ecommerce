## Combining web service calls with Actors and Futures 

Akka actors and Scala concurrency are effective tools for creating concurrent applications. Scala Futures are concurrent functions, and provide a way to maninpuate and combine concurrent tasks without blocking (or, as we'll see later, provide a way to isolate and manage blocking when working with Futures). Akka actors are essentially lightweight, distributed, concurrent objects, representing state that can be accessed concurrently. Combining the two yields a powerful tool-set for simplifying managing concurrent tasks. 

This post describes how the [Orchestrator](https://github.com/lukewyman/ecommerce/tree/master/orchestrator) in ecommerce uses Akka Actors along with Futures to coordinate requests to microservices. The problem that needs to be solved, is how to coordinate and manage these calls in a way that effectively leverages this opportunity to design a solution that is the most responsive to the user. A given use case often requires that multiple requests be made to the microservices that represent the units of work that comprise the use case. Delegating each microservice request to an actor allows these requests to executed concurrently, which is an effective way of achieving the desired responsiveness.

The microservice APIs in ecommerce are exposed as REST endpoints, so each microservice is represented in this solution by an HTTP Client actor in the Orchestrator module. For insights on how to build an Akka actor that works as an HTTP client using Akka HTTP, take a look at [Creating an HTTP Client Actor](http-client-actor.md). 

The Orchestrator module is the heart of e-commerce. Its REST API is the top-level interface at which all shopping and store management requests are made. Each use case consists of a series of requests to the microservices involved, the responses of which must then be combined in some way to return a meaningful response to the user. For example, the use case of an inventory clerk viewing the status of an order to a wholesaler for a given Product would entail:

+ a GET call to the Receiving microservice to get the Shipment
+ a GET call to the Inventory microservice to get a summary of the state of the Product in Inventory
+ a GET call to the Product microservice to get the Product details for display purposes
+ some crunching to create a unified response view

#### (a diagram showing relationship of Orchestrator and microservices)

A good way to structure a solution to a problem like this, is to create a coordinating actor (in this case, an Orchestrator) for each use case (or, each group of related use cases). The coordinating actor, in turn, has several HTTP Client actors as its children, each representing a microservice.  When the coordinator itself, receives a message to execute a use case, it sends a message to HTTP Client actors representing the relevant microservices. Each HTTP Client actor is, in turn, responsible for managing an HTTP request to a microservice. Each HTTP Client actor then returns a message representing a microservice response to the Orchestrator, which then combines and manipulates the responses to create final response that represents the complete use case. The Orchestrator then returns its response as a message to the Orchestrator REST API.

#### (a diagram of the actor tree)

A caveat to all this, is that composing such a solutuon when working on a non-trivial project means managing complexity. Requests to web services aren't always successful, so error handling is required. Managing concurrency in a production-bound application isn't always straight forward. And, finally, the code required to deal with all this can get a little unwieldy for one little actor class. 

This post will explore building out the solution step-by-step. Finer design considerations and the tools to address them will be introduced along the way. We will consider a design to address the complexity of managing concurrency and error handling. The code for this can get a little unwieldly, so we'll take a look at [Cats](http://typelevel.org/cats/), a lightweight, modular library that will help manage this complexity through a functional programming approach. We'll also explore how best practices and refactoring out abstractions make our code more manageable and easier to understand. Finally, we'll take a deeper look at how to manage concurrency in a an Akka application, and see how a custom dispatcher can help.

### The basic structure

The `ReceivingOrchestrator`, which is the coordinating actor for this exercise, will handle the grouping of use cases relevant to the Receiving department of ecommerce. The `ReceivingOrchestrator` requires three HTTP Client actors as children - an `InventoryHttpClient` for the Inventory microservice, a `ProductHttpClient` for the Product microservice, and a `ReceivingHttpClient` for the Receiving microservice. So a start to the `ReceivingOrchestrator` might look something like this:

```scala
import akka.actor.{Actor, Props}
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

class ReceivingOrchestrator(implicit val timeout: Timeout) extends Actor { // The Timeout sets the time in which Futures must complete

  implicit def executionContext: ExecutionContext = context.dispatcher  // The ExecutionContext for completing the Futures

  val inventoryClient = context.actorOf(InventoryHttpClient.props)
  val productClient = context.actorOf(ProductHttpClient.props)
  val receivingClient = context.actorOf(ReceivingHttpClient.props)

  def receive = ???

}
```

### Making the calls

Working with Futures in actors is pretty straight forward. The `ReceivingOrchestrator` actor asks an Http Client actor for a REST response by sending it a message with the `ask` pattern. Using `ask` always returns a Future of the result:
 
 ```scala
 
 val gs: Future[ShipmentView] = receivingClient.ask(GetShipment(sid)).mapTo[ShipmentView]
 
 ```
 
 `gs` is a Future[ShipmentView], which takes as long to complete as it takes for the `ReceivingHttpClient` actor to send a reply. Since futures form [Monads](https://en.wikipedia.org/wiki/Monad_(functional_programming)) (i.e. they have a method called `flatMap`), we can manipulate and combine several Futures in a 'Monadic flow', which results in a Future containing our final result. In this case, we want to `ask` the `ReceivingHttpClient`, the `InventoryHttpClient` and the `ProductHttpClient` for the responses we'll need to create a combined response that displays the status of the Shipment, along with the status of the Product in Inventory, and the Product details. Each of these `ask`s is represented as a Future. `mapToReceivingSummaryView` is a function that takes the responses from each of the Http Client actors (a `ShipmentView`, an `InventoryItemView` and a `ProductView`)  and combines them to create a `ReceivingSummaryView`, the response that will be returned to the sender. The result will, itself, be a Future. So, our Monadic flow looks something like:
 
 ```scala
 
 val result: Future[ReceivingSummaryView] = for {
         gs <- receivingClient.ask(GetShipment(sid)).mapTo[ShipmentView]  // each call returns a Future with a response
         gi <- inventoryClient.ask(GetItem(ProductRef(gs.productId))).mapTo[InventoryItemView]
         gp <- productClient.ask(GetProductByProductId(ProductRef(gs.productId))).mapTo[ProductView]
       } yield mapToReceivingSummaryView(gs, gi, gp)
       
 ```
 
 Finally, Akka provides a `pipe` pattern to send the result of the `Future[ReceivingSummaryView]` to the sender, in this case, the Akka HTTP REST API. It is important to note that this is the point at which the Future blocks. The `pipe` pattern waits for the Future to complete, before sending the result, a naked `ReceivingSummaryView` to the sender. Putting this all together, gives us:
  

```scala
class ReceivingOrchestrator(implicit timeout: Timeout) extends Actor {  
  import akka.pattern.ask
  import akka.pattern.pipe
  // other imports...

  implicit def executionContext: ExecutionContext = context.dispatcher 

  // the ActorRefs of the HttpClient actors
  val inventoryClient = context.actorOf(InventoryHttpClient.props)
  val productClient = context.actorOf(ProductHttpClient.props)
  val receivingClient = context.actorOf(ReceivingHttpClient.props)

  def receive = {
    case GetShipmentSummary(sid) =>
      val result: Future[ReceivingSummaryView] = for {
        gs <- receivingClient.ask(GetShipment(sid)).mapTo[ShipmentView]  // each call returns a Future with a response
        gi <- inventoryClient.ask(GetItem(ProductRef(gs.productId))).mapTo[InventoryItemView]
        gp <- productClient.ask(GetProductByProductId(ProductRef(gs.productId))).mapTo[ProductView]
      } yield mapToReceivingSummaryView(gs, gi, gp)

      // pipe pattern to complete the Future and send the results to the sender
      result.pipeTo(sender()) 
  }
}
```

This solves the core problem. We've composed a basic structure that:
+ Constructs a coordinating actor with HTTP Client actors as children
+ Creates a Future for each microservice request using the `ask` pattern
+ Combines the Futures representing the microservice requests into a single Future of the final response using a Monadic flow
+ Completes the Future and sends the final response to the sender using the `pipe`

### Handling errors from the HTTP clients

All of this is just a little too "perfect world."  The solution, thus far, doens't acccount for the microservices returning errors for resources not found, internal server errors or any multitude of reasons. As it turns out, the HTTP Client actors _do_ return responses that handle error scenarios. Specifically, they return a type of `Either[HttpClientError, T]`. The `Left` of the `Either` is of type `HttpClientError`, while the `Right` contains a successful response `[T]`. 

```scala
def getShipment(shipmentId: ShipmentRef): Future[Either[HttpClientError, ShipmentView]] =
    receivingClient.ask(GetShipment(shipmentId)).mapTo[HttpClientResult[ShipmentView]]
```



#### The EitherT Monad Transformer from Cats

```scala
class ReceivingOrchestrator(implicit timeout: Timeout) extends Actor {
  
  ...
  
  def receive = {
    case GetShipmentSummary(sid) =>
      val result = for {
        gs <- EitherT(receivingClient.ask(GetShipment(sid)).mapTo[Either[HttpClientError, ShipmentView]])
        gi <- EitherT(inventoryClient.ask(GetItem(ProductRef(gs.productId))).mapTo[Either[HttpClientError, InventoryItemView]])
        gp <- EitherT(productClient.ask(GetProductByProductId(ProductRef(gs.productId))).mapTo[Either[HttpClientError, ProductView]])
      } yield mapToReceivingSummaryView(gs, gi, gp)

      result.value.pipeTo(sender()) 
  }
}
```

#### Other things Cats can do

```scala
class ReceivingOrchestrator(implicit timeout: Timeout) extends Actor {
  
  ...
  
  def receive = {
    case GetShipmentSummary(sid) =>
      val result = for {
        gs <- EitherT(receivingClient.ask(GetShipment(sid)).mapTo[Either[HttpClientError, ShipmentView]])
        gi <- EitherT(inventoryClient.ask(GetItem(ProductRef(gs.productId))).mapTo[Either[HttpClientError, InventoryItemView]])
        gp <- EitherT(productClient.ask(GetProductByProductId(ProductRef(gs.productId))).mapTo[Either[HttpClientError, ProductView]])
      } yield mapToReceivingSummaryView(gs, gi, gp)

      result.value.pipeTo(sender()) 

    case ReceivingOrchestrator.AcceptShipment(pid, sid, d, c) =>
      val as = EitherT(receivingClient.ask(ReceivingProtocol.AcceptShipment(sid)).mapTo[HttpClientResult[ShipmentView]])
      val rs = EitherT(inventoryClient.ask(ReceiveSupply(pid, sid, d, c)).mapTo[HttpClientResult[InventoryItemView]])
      val gp = EitherT(productClient.ask(GetProductByProductId(pid)).mapTo[Either[HttpClientError, ProductView]])
      val result = Applicative[EitherT[Future, HttpClientError, ?]].map3(as, rs, gp)(mapToReceivingSummaryView)

      result.value.pipeTo(sender())
  }
}
```

# rework this last paragraph before "The client APIs"...

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

  def acknowledgeShipment(shipmentId: ShipmentRef, expectedDelivery: ZonedDateTime): Future[HttpClientResult[ShipmentView]] =
    receivingClient.ask(AcknowledgeShipment(shipmentId, expectedDelivery)).mapTo[HttpClientResult[ShipmentView]]

  // other calls to receiving Http Client actor...
}

```

Similarly, a trait for each of the `ProductHttpClient` and `InventoryHttpClient` actors need to be created. All three traits are mixed into the `ReceivingOrchestrator` actor. The result is a much cleaner coordinate actor:

```
class ReceivingOrchestrator(implicit val timeout: Timeout) extends Actor
   with InventoryApi
   with ProductApi
   with ReceivingApi {
   import akka.pattern.pipe
   // other imports...
 
   implicit def executionContext: ExecutionContext = context.dispatcher 
 
   def receive = {
     case GetShipmentSummary(sid) =>
       val result = for {
         gs <- EitherT(getShipment(sid))  // each call to an HTTP Client is replaced with a method call to the API trait
         gi <- EitherT(getInventoryItem(ProductRef(gs.productId)))
         gp <- EitherT(getProductByProductId(ProductRef(gs.productId)))
       } yield mapToReceivingSummaryView(gs, gi, gp)
 
       result.value.pipeTo(sender()) 
 
     case AcceptShipment(pid, sid, d, c) =>
       val as = EitherT(acceptShipment(sid))
       val rs = EitherT(receiveSupply(pid, sid, d, c))
       val gp = EitherT(getProductByProductId(pid))
       val result = Applicative[EitherT[Future, HttpClientError, ?]].map3(as, rs, gp)(mapToReceivingSummaryView)
       
       result.value.pipeTo(sender())
   }
 }
```



### Creating and configuring a blocking dispatcher

```json
orchestrator-dispatcher {
  type = Dispatcher
  executor = "thread-pool-executor"
  thread-pool-executor {
    // this configuration is as of Akka 2.4.2
    fixed-pool-size = 16
  }
  throughput = 100
}
```

```
class ReceivingOrchestrator(implicit val timeout: Timeout) extends Actor
   with InventoryApi
   with ProductApi
   with ReceivingApi {
   import akka.pattern.pipe
   // other imports...
 
   implicit def executionContext = context.system.dispatchers.lookup("orchestrator-dispatcher")
   
 
```

### And that's a wrap!

# Summarize what the article is about...
# Consider adding links to documentation resources here..
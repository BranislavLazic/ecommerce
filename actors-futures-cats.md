## Combining web service calls with Actors and Futures 

Akka actors and Scala `Future`s are effective tools for creating concurrent applications. Scala Futures are concurrent functions, and provide a way to maninpulate and combine concurrent tasks without blocking (or, as we'll see later, provide a way to isolate and postpone blocking). Akka actors are essentially lightweight, distributed objects representing concurrent state. Combining the two yields a powerful tool-set for simplifying managing concurrent tasks. 

This post describes how the [Orchestrator](https://github.com/lukewyman/ecommerce/tree/master/orchestrator) in ecommerce uses Akka Actors, along with `Future`s, to coordinate requests to microservices. The problem that needs to be solved, is how to coordinate and manage these calls in a way that effectively leverages this opportunity to design a solution that is the most responsive to the user. A given use case often requires that multiple requests be made to the microservices that represent the units of work that comprise the use case. Delegating each microservice request to an actor allows these requests to executed concurrently, which is an effective way of achieving the desired responsiveness.

The microservice APIs in ecommerce are exposed as REST endpoints, so each microservice is represented in this solution by an HTTP Client actor in the Orchestrator module. For insights on how to build an Akka actor that works as an HTTP client using Akka HTTP, take a look at [Creating an HTTP Client Actor](http-client-actor.md). 

The Orchestrator module is the heart of e-commerce. Its REST API is the top-level interface at which all shopping and store management requests are made. Each use case consists of a series of requests to the microservices involved, the responses of which must then be combined in some way to return a meaningful response to the user. For example, the use case of an inventory clerk viewing the status of an order to a wholesaler for a given Product would entail:

+ a GET call to the Receiving microservice to get the Shipment
+ a GET call to the Inventory microservice to get a summary of the state of the Product in Inventory
+ a GET call to the Product microservice to get the Product details for display purposes
+ some crunching to create a unified response view

#### (a diagram showing relationship of the Orchestrator and microservices)

A good way to structure a solution to a problem like this, is to create a coordinating actor (in this case, an Orchestrator) for each use case (or, each group of related use cases). The coordinating actor, in turn, has several HTTP Client actors as its children, each representing a microservice.  When the coordinator itself, receives a message to execute a use case, it sends a message to HTTP Client actors representing the relevant microservices. Each HTTP Client actor is, in turn, responsible for managing an HTTP request to a microservice, returning a message representing a microservice response to the Orchestrator, which then combines and manipulates the responses to create final response that represents the complete use case. The Orchestrator then returns its response as a message to the Orchestrator REST API.

#### (a diagram of the actor tree)

A caveat to all this, is that composing such a solutuon, when working on a non-trivial project, means managing complexity. Requests to web services aren't always successful, so error handling is required. Managing concurrency in a production-bound application isn't always straight forward. All said, the code required to deal with all this can get a little unwieldy for one little actor class. 

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
 
Each `ask` in the above Monadic flow results in a Future representing the response that will be returned by the corresponding HTTP Client actor. Since Scala `Future`s are non-blocking (until the final Await), this means that three HTTP requests are running _concurrently_, not sequentially. **The time it takes for the entire sequence is, therefore, equal to the time it takes for the longest-running request to complete, and not the sum of the time it takes each request to complete.**
   
 Finally, Akka provides a `pipe` pattern to send the result of the `Future[ReceivingSummaryView]` to the sender, in this case, the Orchestrator REST API. It is important to note that this is the point at which the Future blocks. The `pipe` pattern waits for the Future to complete, before sending the result, a naked `ReceivingSummaryView` to the sender. Putting this all together gives us:
  

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
      // This is where the Future blocks!
      result.pipeTo(sender()) 
  }
}
```

This solves the core problem. We've composed a basic structure that:
+ Constructs a coordinating actor with HTTP Client actors as children
+ Creates a Future for each microservice request using the `ask` pattern, allowing the requests to run concurrently
+ Combines the Futures representing the microservice requests into a single Future of the final response using a Monadic flow
+ Completes the resulting Future and sends the final response to the sender using the `pipe` pattern

### Handling errors from the HTTP clients

All of this is just a little too "perfect world."  The solution, thus far, doens't acccount for the microservices returning errors for resources not found, internal server errors or any multitude of reasons. As it turns out, the HTTP Client actors _do_ return responses that handle error scenarios. Specifically, they return a type of `Either[HttpClientError, T]`. The `Left` of the `Either` is of type `HttpClientError`, while the `Right` contains a successful response of type `T`. Therefore, an `ask` for a microservice response is going to look more like:


```scala

val gs: Future[Either[HttpClientError, ShipmentView]] = receivingClient.ask(GetShipment(sid)).mapTo[Either[HttpClientError, ShipmentView]]

```

The problem becomes one of how to use the result of such a call in a Monadic flow. `Either` is also a Monad, just like `Future`. A `Future[Either[A, B]]` is a Monad inside a Monad. And, because they're Monads of different types, they can't be joined; one`flatMap` alone  won't work. We would have to nest `flatMap` calls two-deep: once to get inside the `Future`, and once again to get inside the `Either`. While this is doable, it would be quite tricky as a coding exercise, and ultimately would dedicate more effort to nested Monads, than to the problem we're trying to solve. We need some construct outside the standard Scala library.

#### The EitherT Monad Transformer from Cats

[Cats](http://typelevel.org/cats/) is a library that provides type classes and algebraic data types that serve as abstractions for functional programming. A full discussion of Cats and functional structures is beyond the scope of this post, but if you're the type to take a deep dive, the Cats documentation, as well as _[Functional Programming in Scala](https://www.manning.com/books/functional-programming-in-scala)_ by Paul Chiusano and Runar Bjarnason are a worthwhile read. 

The data type from the Cats library that will help here, is the `EitherT`. `EitherT` is a Monad Transformer for the `Either` Monad, and any other Monad of choice used to wrap the `Either`. Since this problem deals with an `Either` wrapped in a `Future`, an `EitherT[Future, HttpClientError, T]` will work well. The `apply` of `EitherT` creates an instance of the Monad Transformer:

```scala

val gs: EitherT[Future, HttpClientError, ShipmentView] = EitherT(receivingClient.ask(GetShipment(sid)).mapTo[Either[HttpClientError, ShipmentView]])
```

The magic of `EitherT` is its `flatMapF` method, a super-powered `flatMap` which cuts through both Monad layers, working just like `flatMap` on our original `Future`. The original `Future[Either]]` construct inside `EitherT` is accessed by `EitherT.value`. This allows us to deal with the `Either[HttpClientError, T]` returned by the HTTP Client actors with a Monadic flow, just as we did with plain `Future`s. The result is a clean, concise solution, that is (just about) as readable as the solution we had before we introduced error handling:


```scala
class ReceivingOrchestrator(implicit timeout: Timeout) extends Actor {
  
  ...
  
  def receive = {
    case GetShipmentSummary(sid) =>
      val result = for {
      
      // wrap each 'ask' call with EitherT.apply
        gs <- EitherT(receivingClient.ask(GetShipment(sid)).mapTo[Either[HttpClientError, ShipmentView]])
        gi <- EitherT(inventoryClient.ask(GetItem(ProductRef(gs.productId))).mapTo[Either[HttpClientError, InventoryItemView]])
        gp <- EitherT(productClient.ask(GetProductByProductId(ProductRef(gs.productId))).mapTo[Either[HttpClientError, ProductView]])
      } yield mapToReceivingSummaryView(gs, gi, gp)

      // access the [Future[Either[A, B]] inside the EitherT via result.value before piping to sender
      result.value.pipeTo(sender()) 
  }
}
```

#### Other things Cats can do

The Monad is a powerful functional proramming construct. Monadic flows allow us to string a series of `flatMap`s together, while accessing the context of the Monads involved to "decide" what should happen in the next `flatMap`. In the above example, the second `flatMap` gets a Product based on the productId from the Shipment aquired by the first `flatMap`. The Product returned by the second `flatMap` is decided by the context of the first `flatMap`. 

This power isn't always necessary. For instance, what if we don't need to decide flow control. In the Accept Shipment use case, three microservice responses are simply combined independent of any knowledge of their contents. Following the principle of least power, the Cats library provides a type class called `Applicative`. Applicative is less powerful than Monad, and doesn't have `flatMap`. It does, however, have a series of `map` methods, with arity of `map` through `map22`. Since the Accept Shipment use case requires combining three responses, `Applicative.map3(fa, fb, fc)(f: (a, b, c) => d)` should work just fine. In our case, `map3` takes three `EitherT`s and a function that takes three arguments - the `mapToReceivingSummaryView` that we've been using all along:

```scala
class ReceivingOrchestrator(implicit timeout: Timeout) extends Actor {
  
  ...
  
  def receive = {
    case GetShipmentSummary(sid) =>
      
      ...

    case ReceivingOrchestrator.AcceptShipment(pid, sid, d, c) =>
    
      // retrieve each EitherT with a discreet 'ask'
      val as = EitherT(receivingClient.ask(ReceivingProtocol.AcceptShipment(sid)).mapTo[HttpClientResult[ShipmentView]])
      val rs = EitherT(inventoryClient.ask(ReceiveSupply(pid, sid, d, c)).mapTo[HttpClientResult[InventoryItemView]])
      val gp = EitherT(productClient.ask(GetProductByProductId(pid)).mapTo[Either[HttpClientError, ProductView]])
      
      // Use the Applicative to apply the EitherT's to mapToReceivingSummaryView
      val result = Applicative[EitherT[Future, HttpClientError, ?]].map3(as, rs, gp)(mapToReceivingSummaryView)

      result.value.pipeTo(sender())
  }
}
```

Not bad, so far. We have a workable structure that combines multiple, concurrent requests to microservices. We've also used Cats to provide an elegant approach to handling concurrency and error handling simultaneously. But, if we're going to be doing a lot of this, all those HTTP Client calls are going to become tedious and repetitive. The HTTP Client actors are going to be reused in other groupings of use cases (Orchestrators), meaning that the current path is one to code duplication. The `ShoppingOrchestrator`, for example, will also use the `ProductHttpClient` and the `InventoryHttpClient` actors. Also, the code to prepare the messages and do the asking gets cumbersome and in the way. It would be nice to abstract away the child actors and the `ask` pattern into a set of reusable client API traits.

### The client APIs

A clean design to achieve this, would be to separate the `ask` calls into a series of traits - one for each HTTP Client actor. Where, before, getting a `Future[SomeResponseView]` would be done with the `ask` pattern and a child actor, the `ReceivingOrchestrator` would simply mix in the client API trait and call a method like `getShipment(shipmentId)`. The HTTP Client actor would also be instantiated in the trait. Here is the `ReceivingApi` trait as an example:

```scala

trait ReceivingApi { this: Actor =>  // makes Actor characteristics available to this trait
  import akka.pattern.ask
  // other imports...

  // the ExecutionContext and Timeout are still implemented in the ReceivingActor class
  implicit def executionContext: ExecutionContext
  implicit def timeout: Timeout

  // each child actor instance is moved to its trait
  val receivingClient = context.actorOf(ReceivingHttpClient.props)

  // these were the same 'ask' statements in the ReceivingActor, implemented here as methods
  // that take the message contents from the actor as parameters
  def getShipment(shipmentId: ShipmentRef): Future[HttpClientResult[ShipmentView]] =
    receivingClient.ask(GetShipment(shipmentId)).mapTo[HttpClientResult[ShipmentView]]  

  def acknowledgeShipment(shipmentId: ShipmentRef, expectedDelivery: ZonedDateTime): Future[HttpClientResult[ShipmentView]] =
    receivingClient.ask(AcknowledgeShipment(shipmentId, expectedDelivery)).mapTo[HttpClientResult[ShipmentView]]

  // other calls to receiving Http Client actor...
}

```

Similarly, a client API trait is created for each of the `ProductHttpClient` and `InventoryHttpClient` actors. All three traits are mixed into the `ReceivingOrchestrator` actor, resulting in a much cleaner coordinator actor:

```
class ReceivingOrchestrator(implicit val timeout: Timeout) extends Actor
   with InventoryApi  // The client API traits are mixed into the ReceivingOrchestrator actor
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

There is one last nut left to crack before we can call it a day. As discussed, the `pipeTo` statement is the point at which the Future blocks. The actor has to wait on the current thread until the Future completes, before the combined response is returned to the sender. Up till now, the `ReceivingOrchestrator` has been using the `context.dispatcher` to complete Futures. The problem with this, is that the `context.dispatcher` serves as the same Execution Context as the routing infrastructure that the Akka system uses to coordinate communication between actors. If too many threads are blocked in this way, the actor system itself is starved, and the benefit of Akka as a concurrency management runtime is lost. The actor might perform well in unit tests, but performance under load would be problematic.

The solution to this problem, is to configure a custom blocking dispatcher file, and use it in place of the context.dispatcher in the `ReceivingOrchestrator`. The actor system allows for a lookup of custom configured dispatchers with a call to `system.dispatcher.lookup("dispatcher-name")`. We'll call our dispatcher `orchestrator-dispatcher` and configure it in the application.conf file:

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

Replacing the context.dispatcher with the custom `orchestrator-dispatcher` in the `ReceivingActor` means that blocking is now isolated to a dedicated dispatcher:

```
class ReceivingOrchestrator(implicit val timeout: Timeout) extends Actor
   with InventoryApi
   with ProductApi
   with ReceivingApi {
   import akka.pattern.pipe
   // other imports...
 
   implicit def executionContext = context.system.dispatchers.lookup("orchestrator-dispatcher")
   
 
```

The Akka documentation provides a [thorogh examination[(http://doc.akka.io/docs/akka-http/10.0.0/scala/http/handling-blocking-operations-in-akka-http-routes.html) of this topic. For now, the `ReceivingOrchestrator` is ready to call microservices and process requests for the Receiving Department of ecommerce.


### And that's a wrap!

Building a coordinating actor that uses child actors to coordinate and combine HTTP requests has demonstrated an effective way to use the power of Akka actors and Scala `Future`s to manage concurrent tasks. Doing this to solve a real world problem and build a `ReceivingOrchestrator` has proven to also be a complex project. Not only did we have to deal with the obvious work of manipulating `Future`s, but also with the reality of errors as responses. Using Scala as a functional language makes for a concise solution for managing concurrency and error handling in one swoop. When the going gets tough, a functional library like Cats can be of great help. Refactoring the code into reusable API traits makes the messaging coordination code more expressive. Finally, we learned how to isolate blocking by configuring and deploying a custom dispatcher, rendering a more scalable application.

Along the way, it appears we may have also learned what an actor system really is: a runtime for building concurrent applications. All an Akka actor is supposed to do, is send and receive messages, essenstially serving as the "scaffolding" for such an application. Using effective functional programming techniques and best practices reveals this intent.
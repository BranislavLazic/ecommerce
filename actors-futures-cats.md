### Combining web service calls with Actors and Futures 

Akka actors and Scala concurrency are effective tools for creating concurrent applications. Scala Futures represent concurrent functions, and provide a way to maninpuate and combine concurrent tasks without blocking. Akka actors are essentially lightweight concurrent objects, multiple tasks to be carried out concurrently. Combining the two...

The Orchestrator is the heart of e-commerce. Its REST API is the to which all shopping and store management requests are made. Each use case will usually consist of a series of web service calls to the applicable microservices. For example, the use case of an inventory clerk viewing the status of an order to a wholesaler for a given Product would entail:

+ a GET call to the Receiving microservice to get the Shipment
+ a GET call to the Inventory microservice to get a summary of state of the Product in Inventory
+ a GET call to the Product microservice to get the product details for display purposes
+ some crunching to create a unified response view





```scala
trait ProductApi { this: Actor =>
  // imports...
  
  implicit def executionContext: ExecutionContext
  implicit def timeout: Timeout

  def productClient = context.actorOf(ProductHttpClient.props)

  def getProductByProductId(productId: ProductRef): Future[HttpClientResult[ProductView]]  =
    productClient.ask(GetProductByProductId(productId)).mapTo[HttpClientResult[ProductView]]

  def getProductsByCategoryId(categoryId: CategoryRef): Future[HttpClientResult[List[ProductView]]] =
    productClient.ask(GetProductsByCategory(categoryId)).mapTo[HttpClientResult[List[ProductView]]]
    
  // other methods
}   
```

### Coordinating web service calls with Actors and Futures 

The Orchestrator is the heart of e-commerce. Its REST API is the to which all shopping and store management requests are made. Each use case will usually consist of a series of web service calls to the applicable microservices. For example, the use case for a customer removing an item from their shopping cart would consist of:

+ A DELETE call to the shoppiing cart microservice to remove the item
+ A POST call to the 
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

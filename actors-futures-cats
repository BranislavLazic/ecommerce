### This is a code sample

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

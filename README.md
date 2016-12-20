#e-commerce#

e-commerce is a sample project to show-case my skills in Scala and Akka. This project is currently in progress, so I will update this readme on a regular basis to indicate modules that are ready for review, the basic approach to each module, and a summary of the skills used. Other nice things, like how to setup and run the app and the scripts to do it all will follow a little later.

##Architecture##
The high level view of e-commerce is based on Domain-Driven Design, implemented through the Microservices approach. Each module is designed as a microservice that does one thing and does it well. 

###Modules###

####Microservices####
* **ordertracking** - (_not done_) - this will be the core domain of the e-commerce project and will manage the lifecycle of an online shopping order from initiation to completion
* **shoppingcart** - manages basic shopping cart functionality. This is an Akka/Scala app with an Akka HTTP api. Akka features used include Persistence, Cluster Sharding. and Akka HTTP
* **inventory** - (_in progress_) - is a microsevice built with Akka and Scala to manage the inventory for each product that the business offers. It maintains the current availablity of a product, places holds on inventory for shopping carts that are in progress, Backorders for that classic "we're out of stock, but we'll have more by whenever" scenario are also managed here.
* **productcatalog** - (_not done_)
* **payment** - (_not done_)
* **fulfillment** - (_not done_)
* **shipping** - (_not done_)

####Integration and UI####
* **integration** - (_not done_) will likely be an Akka app that coordinates requests between microservices. For example, if a customer wants to checkout after selecting their items, it would coordinate calls to the **payment** microservice to collect money, the **shoppingcart** microservice to clear their shopping cart, the **inventory** microservice to release the hold(s) on item(s) while the shopping cart was active, and the **ordertracking** microservice to initiate the order with a status of "placed".
* **UI** - (_not done_) this will be a Play app that talks to the **integration** layer. I don't know Play yet, so I have no idea what it's going to look like.


###Bibliography###

I used various resources to both learn Akka itself, as well as for ideas with regard to patterns for Reactive architecture:
* [*Akka in Action*, Raymond Roestenburg, Manning](https://www.manning.com/books/akka-in-action) - Used this to learn Akka. I referenced the sample code in this book for my basic approach to setting up an Akka app in general, as well as for the cluster sharding and persistence.
* [*Reactive Design Patterns*, Roland Kuhn, Manning](https://www.manning.com/books/reactive-design-patterns) - As the title suggests, I referenced the design patterns in this book to organize the Akka code into patterns that organize concepts and make the code more understandable, refactorable and testable.
* [The online Akka documentation](http://doc.akka.io/docs/akka/2.4/scala.html) - My go-to reference.
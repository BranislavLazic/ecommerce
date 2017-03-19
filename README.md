# e-commerce #

e-commerce is a resume portfolio project to show-case my skills in Scala and Akka, using the old-as-time domain of the online shopping site. This project is currently in progress, so I will update this readme on a regular basis to update prospective viewers on my progress. Feel free to hit me up on LinkedIn if you like what you see!

## Motivation and Strategy ##

I'm open to the criticism that this project is a little too ambitious for a single developer. The idea is to create a big playground for myself to romp around in as I grow my skills in Scala, Akka and related technologies. Up till now, I've been building out different areas of the solution as I learn different aspects of Akka. I'm getting to the point where I'm about to start standing up various microservices and getting it to run as an integrated solution. Once I have that wrapped up, I'll start working on the DevOps side of things with Docker, AWS and the like. I'm a little behind on unit tests, but working on getting to complete coverage.

## Skills Demonstrated ##
* Scala
* [cats](https://github.com/typelevel/cats)
* Akka
* Akka HTTP
* REST
* Kafka


## Architecture ##
The high level view of e-commerce is based on Domain-Driven Design, implemented through the Microservices approach. Each module is designed as a microservice that does one thing and does it well. I don't plan on adding a UI to the project, as it is intended to demo backend dev skills.

### Orchestrator (Integration) ###
The Orchestrator module is the command-and-control of the project, and it's API is what a potential front-end would communicate with. The REST API uses Akka HTTP. The Orchestrator actors demonstrate concurrency with Scala Futures. Cats typeclasses make the Scala code that manages Futures and Either microservice responses nice and clean.

### Microservices (Modules) ###
Each module in ecommerce is represented by a microservice, implemented as an Akka application. Each application has a REST API, implemented with Akka HTTP.

* **product-catalog** - uses single-use Akka Actors and Slick to retreive product information from a MySql database.
* **shoppingcart** - Clustered persistent actors that represent shopping carts
* **inventory** - Clustered persistent actors that represent the in-stock and backorder quantities for a given product. Holds for customers that have placed the item in their carts are also managed here.
* **receiving** - Clustered persistent actors that represent in-bound shipments to replenish product supply. This microservice will likely remain dead simple. It's mostly here to demonstrate the orchestrator coordinating calls to Inventory.
* **order-tracking** - (skeleton and some REST API) - will manage the status of an order as it changes state.
* **shipping** - (not done) - vague idea of using third party APIs to submit outbound shipments to UPS, USPS, etc.
* **fulfillment** - (not done) - will manage order item fulfilments as they happen in the warehouse.
* **payment** - (not done) - no idea what this will look like. I'd like to do something with third party APIs for Visa and PayPal, etc.

### Bibliography ###

I used various resources to both learn Akka itself, as well as for ideas with regard to patterns for Reactive architecture:
* [*Akka in Action*, Raymond Roestenburg, Manning](https://www.manning.com/books/akka-in-action) - Used this to learn Akka. I referenced the sample code in this book for my basic approach to setting up an Akka app in general, as well as for the cluster sharding and persistence.
* [*Reactive Design Patterns*, Roland Kuhn, Manning](https://www.manning.com/books/reactive-design-patterns) - As the title suggests, I referenced the design patterns in this book to organize the Akka code into patterns that organize concepts and make the code more understandable, refactorable and testable.
* [The online Akka documentation](http://doc.akka.io/docs/akka/2.4/scala.html) - My go-to reference.
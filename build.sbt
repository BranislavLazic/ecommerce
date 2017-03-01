lazy val ecommerce =
  project
    .in(file("."))
    .aggregate(customers, fulfillment, inventory, `order-tracking`, payment, `product-catalog`,
      receiving, shipping, shoppingcart, orchestrator)


lazy val commonSettings = Seq(
  scalaVersion := Version.scala,
  libraryDependencies ++=
    Groupings.akkaBasics ++
    Groupings.akkaHttp ++
    Groupings.circe
)

lazy val common = project.in(file("common")).settings(commonSettings)

lazy val orchestratorSettings = Seq(
  scalaVersion := Version.scala,
  mainClass in Global := Some("com.ecommerce.orchestrator.Boot"),
  assemblyJarName in assembly := "orchestrator.jar",
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.3"),
  libraryDependencies ++=
    Groupings.akkaBasics ++
    Groupings.akkaHttp ++
    Groupings.circe ++
    Seq(
      Library.scalaTest % "test",
      Library.jodaTime,
      Library.cats
    )
)

lazy val orchestrator = project.in(file("orchestrator")).settings(orchestratorSettings).dependsOn(common)

lazy val customersSettings = Seq(
  scalaVersion := Version.scala,
  mainClass in Global := Some("com.ecommerce.customers.Boot"),
  assemblyJarName in assembly := "customers.jar",
  libraryDependencies ++= Seq(
    Library.akkaActor,
    Library.akkaHttp,
    Library.akkaHttpCirce
  )
)

lazy val customers = project.in(file("customers")).settings(customersSettings)


lazy val fulfillmentSettings = Seq(
  scalaVersion := Version.scala,
  mainClass in Global := Some("com.ecommerce.fulfillment.Boot"),
  assemblyJarName in assembly := "fulfillment.jar",
  libraryDependencies ++= Seq(
    Library.akkaActor,
    Library.akkaHttp,
    Library.akkaHttpCirce
  )
)

lazy val fulfillment = project.in(file("fulfillment")).settings(fulfillmentSettings)


lazy val inventorySettings = Seq(
  scalaVersion := Version.scala,
  assemblyJarName in assembly := "inventory.jar",
  resolvers ++= Seq("Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    "Sonatype snapshots"  at "http://oss.sonatype.org/content/repositories/snapshots/"),
  libraryDependencies ++=
    Groupings.akkaBasics ++
    Groupings.akkaPersistence ++
    Groupings.akkaCluster ++
    Groupings.akkaHttp ++
    Groupings.circe  ++
    Seq(
      Library.scalaTest % "test"
    )
)

lazy val inventory = project.in(file("inventory")).settings(inventorySettings).dependsOn(common)


lazy val orderTrackingSettings = Seq(
  scalaVersion := Version.scala,
  mainClass in Global := Some("com.ecommerce.ordertracking.Boot"),
  assemblyJarName in assembly := "ordertracking.jar",
  libraryDependencies ++=
    Groupings.akkaBasics ++
    Groupings.akkaCluster ++
    Groupings.akkaPersistence ++
    Groupings.akkaHttp ++
    Groupings.circe
)

lazy val `order-tracking` = project.in(file("order-tracking")).settings(orderTrackingSettings)


lazy val paymentSettings = Seq(
  scalaVersion := Version.scala,
  mainClass in Global := Some("com.ecommerce.payment.Boot"),
  assemblyJarName in assembly := "payment.jar",
  libraryDependencies ++=
    Groupings.akkaBasics ++
    Groupings.akkaHttp ++
    Groupings.circe
)

lazy val payment = project.in(file("payment")).settings(paymentSettings)


lazy val productcatalogSettings = Seq(
  scalaVersion := Version.scala,
  mainClass in Global := Some("com.ecommerce.productcatalog.Boot"),
  assemblyJarName in assembly := "productcatalog.jar",
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.3"),
  libraryDependencies ++=
    Groupings.akkaBasics ++
    Groupings.akkaHttp ++
    Groupings.circe ++
    Groupings.slick ++
    Seq(
      Library.scalaTest % "test",
      Library.wixMysql  % "test"
    )
)

lazy val `product-catalog` = project.in(file("product-catalog")).settings(productcatalogSettings).dependsOn(common)


lazy val receivingSettings = Seq(
  scalaVersion := Version.scala,
  mainClass in Global := Some("com.ecommerce.receiving.Boot"),
  assemblyJarName in assembly := "receiving.jar",
  libraryDependencies ++=
    Groupings.akkaBasics ++
    Groupings.akkaPersistence ++
    Groupings.akkaCluster ++
    Groupings.akkaHttp ++
    Groupings.circe
)


lazy val receiving = project.in(file("receiving")).settings(receivingSettings).dependsOn(common)


lazy val shippingSettings = Seq(
  scalaVersion := Version.scala,
  mainClass in Global := Some("com.ecommerce.shipping.Boot"),
  assemblyJarName in assembly := "shipping.jar",
  libraryDependencies ++=
    Groupings.akkaBasics ++
      Groupings.akkaPersistence ++
      Groupings.akkaHttp ++
      Groupings.circe
)

lazy val shipping = project.in(file("shipping")).settings(shippingSettings)


lazy val shoppingcartSettings = Seq(
  scalaVersion := Version.scala,
  mainClass in Global := Some("com.ecommerce.shoppingcart.Boot"),
  assemblyJarName in assembly := "shoppingcart.jar",
  resolvers ++= Seq("Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    "Sonatype snapshots"  at "http://oss.sonatype.org/content/repositories/snapshots/"),
  libraryDependencies ++=
    Groupings.akkaBasics ++
    Groupings.akkaCluster ++
    Groupings.akkaPersistence ++
    Groupings.akkaHttp ++
    Groupings.circe ++
    Seq(
      Library.scalaTest % "test",
      Library.akkaTestKit % "test"
    )
)

lazy val shoppingcart = project.in(file("shoppingcart")).settings(shoppingcartSettings).dependsOn(common)


lazy val ui = project.in(file("ui")).enablePlugins(PlayScala)
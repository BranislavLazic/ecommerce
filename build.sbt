lazy val ecommerce =
  project
    .in(file("."))
    .aggregate(inventory, shoppingcart)

lazy val inventorySettings = Seq(
  scalaVersion := Version.scala,
  assemblyJarName in assembly := "inventory.jar",
  resolvers ++= Seq("Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    "Sonatype snapshots"  at "http://oss.sonatype.org/content/repositories/snapshots/"),
  libraryDependencies ++= Seq(
    Library.akkaActor,
    Library.akkaCluster,
    Library.akkaClusterSharding,
    Library.akkaClusterTools,
    Library.akkaPersistence,
    Library.akkaHttp,
    Library.akkaSlf4j,
    Library.commonsIO,
    Library.logbackClassic,
    Library.akkaHttpCirce,
    Library.circeCore,
    Library.circeGeneric,
    Library.circeParser,
    Library.leveldb,
    Library.leveldbJni,
    Library.jodaTime,
    Library.scalaTest % "test"
  )
)

lazy val inventory = project.in(file("inventory")).settings(inventorySettings)

lazy val shoppingcartSettings = Seq(
  scalaVersion := Version.scala,
  mainClass in Global := Some("com.ecommerce.shoppingcart.Boot"),
  assemblyJarName in assembly := "shoppingcart.jar",
  resolvers ++= Seq("Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    "Sonatype snapshots"  at "http://oss.sonatype.org/content/repositories/snapshots/"),
  libraryDependencies ++= Seq(
    Library.akkaActor,
    Library.akkaCluster,
    Library.akkaClusterSharding,
    Library.akkaClusterTools,
    Library.akkaPersistence,
    Library.akkaHttp,
    Library.akkaSlf4j,
    Library.commonsIO,
    Library.logbackClassic,
    Library.akkaHttpCirce,
    Library.circeCore,
    Library.circeGeneric,
    Library.circeParser,
    Library.leveldb,
    Library.leveldbJni,
    Library.scalaTest % "test"
  )
)

lazy val shoppingcart = project.in(file("shoppingcart")).settings(shoppingcartSettings)

lazy val orchestratorSettings = Seq(
  scalaVersion := Version.scala,
  mainClass in Global := Some("com.ecommerce.orchestrator.Boot"),
  assemblyJarName in assembly := "orchestrator.jar",
  resolvers ++= Seq("Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    "Sonatype snapshots"  at "http://oss.sonatype.org/content/repositories/snapshots/"),
  libraryDependencies ++= Seq(
    Library.akkaActor,
    Library.akkaHttp,
    Library.akkaSlf4j,
    Library.akkaHttpCirce,
    Library.circeCore,
    Library.circeGeneric,
    Library.circeParser,
    Library.jodaTime
  )
)

lazy val orchestrator = project.in(file("orchestrator")).settings(orchestratorSettings)

lazy val ui = project.in(file("ui")).enablePlugins(PlayScala)
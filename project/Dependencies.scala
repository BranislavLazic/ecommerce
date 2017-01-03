import sbt._

object Version {
  final val scala = "2.12.1"
  final val akka = "2.4.14"
  final val akkaHttp = "10.0.0"
  final val akkaHttpCirce = "1.11.0"
  final val akkaPersistenceCassandra = "0.22"
  final val scalaTest = "3.0.0"
  final val commonsIO = "2.4"
  final val logbackClassic = "1.1.2"
  final val circe = "0.6.1"
  final val leveldb = "0.7"
  final val leveldbJni = "1.8"
  final val jodaTime = "2.9.6"
}

object Library {
  val akkaActor                = "com.typesafe.akka"          %%  "akka-actor"                      % Version.akka
  val akkaPersistence          = "com.typesafe.akka"          %%  "akka-persistence"                % Version.akka
  val akkaCluster              = "com.typesafe.akka"          %%  "akka-cluster"                    % Version.akka
  val akkaClusterTools         = "com.typesafe.akka"          %%  "akka-cluster-tools"              % Version.akka
  val akkaClusterSharding      = "com.typesafe.akka"          %%  "akka-cluster-sharding"           % Version.akka
  val akkaSlf4j                = "com.typesafe.akka"          %%  "akka-slf4j"                      % Version.akka
  val akkaPersistenceCassandra = "com.typesafe.akka"          %% "akka-persistence-cassandra"       % Version.akkaPersistenceCassandra
  val commonsIO                = "commons-io"                 %   "commons-io"                      % Version.commonsIO
  val logbackClassic           = "ch.qos.logback"             %   "logback-classic"                 % Version.logbackClassic
  val akkaHttp                 = "com.typesafe.akka"          %%  "akka-http"                       % Version.akkaHttp
  val akkaHttpCirce            = "de.heikoseeberger"          %%  "akka-http-circe"                 % Version.akkaHttpCirce
  val circeCore                = "io.circe"                   %%  "circe-core"                      % Version.circe
  val circeGeneric             = "io.circe"                   %%  "circe-generic"                   % Version.circe
  val circeParser              = "io.circe"                   %%  "circe-parser"                    % Version.circe
  val circeJava8               = "io.circe"                   %%  "circe-java8"                     % Version.circe
  val jodaTime                 = "joda-time"                  %   "joda-time"                       % Version.jodaTime

  val akkaTestKit              = "com.typesafe.akka"          %%  "akka-testkit"                    % Version.akka
  val akkaMultiNodeTestkit     = "com.typesafe.akka"          %%  "akka-multi-node-testkit"         % Version.akka
  val scalaTest                = "org.scalatest"              %%  "scalatest"                       % Version.scalaTest

  val leveldb                  = "org.iq80.leveldb"           %   "leveldb"                       % Version.leveldb
  val leveldbJni               = "org.fusesource.leveldbjni"  %   "leveldbjni-all"                % Version.leveldbJni
}
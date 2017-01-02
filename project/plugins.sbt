resolvers += Classpaths.typesafeReleases

resolvers += Resolver.bintrayRepo("hseeberger", "maven")

resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/maven-releases/"

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.13.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-multi-jvm" % "0.3.8")

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.10")
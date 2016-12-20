resolvers += Classpaths.typesafeReleases

resolvers += Resolver.bintrayRepo("hseeberger", "maven")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.13.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-multi-jvm" % "0.3.8")
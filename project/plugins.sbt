resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.8")

// web plugins
// cant get it to work, use grunt instead...
addSbtPlugin("com.typesafe.sbt" % "sbt-coffeescript" % "1.0.0")

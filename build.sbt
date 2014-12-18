name := "leaps-play"

version := "0.3"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
  "org.mindrot" % "jbcrypt" % "0.3m",
  cache
)     

play.Project.playScalaSettings
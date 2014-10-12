name := "Leaps"

version := "0.5"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  "org.postgresql" % "postgresql" % "9.3-1102-jdbc41",
  "org.mindrot" % "jbcrypt" % "0.3m",
  cache
)     

play.Project.playScalaSettings

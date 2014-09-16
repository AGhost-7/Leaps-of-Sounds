name := "Leaps"

version := "0.5"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  "mysql" % "mysql-connector-java" % "5.1.18",
  cache
)     

play.Project.playScalaSettings

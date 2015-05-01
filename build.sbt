name := "leaps-play"

version := "0.3"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
  "org.mindrot" % "jbcrypt" % "0.3m",
  cache,
	"org.scalikejdbc" %% "scalikejdbc-async" % "0.5.+",
	"org.scalikejdbc" %% "scalikejdbc-async-play-plugin" % "0.5.+",
	"com.github.mauricio" %% "postgresql-async" % "0.2.+",
	"org.scala-lang.modules" %% "scala-async" % "0.9.2"
)     

lazy val root = (project in file(".")).enablePlugins(SbtWeb, PlayScala)

pipelineStages := Seq(uglify, digest, gzip)



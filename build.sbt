import sbt.Keys._

val akkaVersion = "2.4.12"
val kafkaVersion = "0.10.0.0"
val zookeeperVersion = "3.4.7"
val cassandraVersion = "3.1.0"
val guavaVersion = "19.0"
val mongoVersion = "3.3.0"


lazy val commonSettings = Seq(
  organization := "cl.reactivecloud",
  version := "1.0-SNAPSHOT",
  scalaVersion := "2.11.8",
  resolvers += "Local Maven Repository" at Path.userHome.asFile.toURI.toURL + "/.m2/repository",
  resolvers += DefaultMavenRepository,
  libraryDependencies += "junit" % "junit" % "4.12" % Test,
  libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % Test,
  libraryDependencies += "org.apache.curator" % "curator-framework" % "2.11.0"
)

lazy val akka = (project in file("."))
  .settings(commonSettings: _*)
  .settings(
    name := "reactivecloud-learning",
    libraryDependencies += "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.8.3",
    libraryDependencies += "com.fasterxml.jackson.module" % "jackson-module-scala_2.11" % "2.8.3"
  )

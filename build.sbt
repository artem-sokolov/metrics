name := "metrics"

version := "0.1"

scalaVersion := "2.12.5"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.1.1",
  "com.typesafe.akka" %% "akka-stream" % "2.5.12",
  "org.mapdb" % "mapdb" % "3.0.5",

  "com.typesafe.akka" % "akka-testkit_2.12" % "2.5.12" % "test",
  "org.scalatest" % "scalatest_2.12" % "3.0.5" % "test"
)
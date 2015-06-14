name := "scala-disque"

version := "0.0.1"

scalaVersion := "2.11.6"

// Akka ecosystem
//libraryDependencies ++= Seq(
//  "com.typesafe.akka" %% "akka-actor" % "2.3.11",
//  "com.typesafe.akka" %% "akka-testkit" % "2.3.11" % "test"
//)

libraryDependencies += "net.debasishg" %% "redisclient" % "3.0"

// Library for test scaffolding.
libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test"

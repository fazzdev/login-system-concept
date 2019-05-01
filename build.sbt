scalaVersion := "2.12.8"
name := "login-system-concept"
organization := "fazzdev"
version := "1.0"

libraryDependencies ++= Seq(
  "org.tpolecat" %% "doobie-core" % "0.6.0",
  "org.tpolecat" %% "doobie-postgres" % "0.6.0",
  "org.typelevel" %% "cats-core" % "1.4.0",
  "com.typesafe.akka" %% "akka-actor" % "2.5.22",
  "com.typesafe.akka" %% "akka-stream" % "2.5.22",
  "com.typesafe.akka" %% "akka-http" % "10.1.8",
  "com.typesafe.play" %% "play-json" % "2.7.3",
)

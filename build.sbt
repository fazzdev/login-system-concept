scalaVersion := "2.12.8"
name := "login-system-concept"
organization := "fazzdev"
version := "1.0"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "1.4.0",
  "org.tpolecat" %% "doobie-core" % "0.6.0",
  "org.tpolecat" %% "doobie-postgres" % "0.6.0",
  "com.typesafe.play" %% "play-json" % "2.7.3"
)

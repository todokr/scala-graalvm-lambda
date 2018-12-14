import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.12.5",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "Hello",
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play-json" % "2.6.10",
      "org.scalaj" %% "scalaj-http" % "2.4.1",
      scalaTest % Test
    )
  )

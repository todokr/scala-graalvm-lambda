import Dependencies._
import scala.sys.process._

lazy val buildContainer = taskKey[Unit]("Build docker image of GraalVM")
lazy val runBuildServer = taskKey[Unit]("Run GraalVM server for build native image.")
lazy val nativeCompile = taskKey[Unit]("Build native image compiled by GraalVM")

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "io.github.todokr",
      scalaVersion := "2.12.5",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "Hello",
    libraryDependencies ++= Seq(
      "io.spray" %% "spray-json" % "1.3.5",
      "org.scalaj" %% "scalaj-http" % "2.4.1",
      scalaTest % Test
    ),
    mainClass in assembly := Some("bootstrap.Main"),
    assemblyJarName in assembly := s"scala-graalvm-lamda_${version.value}.jar",
    test in assembly := {},
    buildContainer := {
      val exitCode = ("docker images" #| "grep graal-build-img").!(ProcessLogger(_ => ()))
      if (exitCode == 1) {
        println("Build GraalVM container...")
        "docker build -f Dockerfile -t graal-build-img .".!
      } else println("Container is already built.")
    },
    runBuildServer := {
      buildContainer.value
      val exitCode = ("docker ps" #| "grep graal-builder$").!(ProcessLogger(_ => ()))
      if (exitCode == 1) {
        println("Start build server...")
        "docker run --name graal-builder -dt graal-build-img:latest".!
      } else println("Build server is already running.")
    },
    nativeCompile := {
      clean.value
      assembly.value
      runBuildServer.value
      val jarName = (assemblyJarName in assembly).value
      (s"docker cp target/scala-2.12/$jarName graal-builder:server.jar" #&&
       "time docker exec graal-builder native-image -H:+ReportUnsupportedElementsAtRuntime -H:EnableURLProtocols=http,https -J-Xmx3G -J-Xms3G --no-server -jar server.jar" #&&
       "docker cp graal-builder:server target/bootstrap" #&&
       "docker cp graal-builder:/opt/graalvm-ce-1.0.0-rc15/jre/lib/amd64/libsunec.so target/libsunec.so" #&&
       "zip -j target/bundle.zip target/bootstrap target/libsunec.so").!
    }
  )
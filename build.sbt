import Dependencies._
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport.{dockerRepository, dockerUpdateLatest}

enablePlugins(JavaAppPackaging)
enablePlugins(UniversalPlugin)
enablePlugins(DockerPlugin)

name := "test-populator"
organization := "io.sudostream"
scalaVersion := "2.11.8"
version := "0.0.28-2"

//docker
dockerBaseImage := "anapsix/alpine-java:8_server-jre"
dockerRepository := Some("eu.gcr.io/time-to-teach-zone")
dockerUpdateLatest := true
packageName in Docker := "test-populator"

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.2.1",
  "io.netty" % "netty-all" % "4.1.15.Final",
  "io.argonaut" %% "argonaut" % "6.1",
  "org.mongodb.scala" %% "mongo-scala-driver" % "2.1.0",
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  scalaTest % Test
)

fork in run := true

javaOptions in run ++= Seq(
  "-Djavax.net.ssl.keyStore=/etc/ssl/cacerts",
  "-Djavax.net.ssl.keyStorePassword=the8balL",
  "-Djavax.net.ssl.trustStore=/etc/ssl/cacerts",
  "-Djavax.net.ssl.trustStorePassword=the8balL"
)


import Dependencies._

ThisBuild / scalaVersion     := "2.13.6"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val zioDeps = List(
  "dev.zio" % "zio_2.13"                % "1.0.9",
  "dev.zio" % "zio-metrics_2.13"        % "1.0.12",
  "dev.zio" % "zio-metrics-statsd_2.13" % "1.0.4",
)

lazy val catsDeps = List(
  "org.typelevel"   %% "cats-effect"      % "3.2.2",
  "com.avast.cloud" %% "datadog4s-statsd" % "0.31.0"
)

lazy val root = (project in file("."))
  .settings(
    name := "fp",
    libraryDependencies ++= List(
      scalaTest            % Test,
      "org.scalameta"      % "munit_2.13"  % "0.7.28" % Test,
      "com.lihaoyi"        % "pprint_2.13" % "0.6.6",
      "com.typesafe.slick" % "slick_2.13"  % "3.3.3"
    ).++(catsDeps),
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.

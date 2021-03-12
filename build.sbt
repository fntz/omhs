
val scala12 = "2.12.13"
val scala13 = "2.13.5"
val supportedVersions = Seq(scala12, scala13)

version := "0.0.1-SNAPSHOT"

ThisBuild / organization := "com.github.fntz"
ThisBuild / version      := version.value
ThisBuild / scalaVersion := scala12


val opts = Seq(
  scalaVersion := scala12,
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-language:postfixOps",
    "-language:reflectiveCalls",
    "-unchecked",
    "-Xverify",
    "-Ydelambdafy:inline" // todo https://github.com/scala/bug/issues/10554
  ),
  scalacOptions in Test ++= Seq("-Yrangepos")
)

val netty = Seq("io.netty" % "netty-codec-http" % "4.1.59.Final")

val logback = Seq("ch.qos.logback"  %  "logback-classic"    % "1.2.3")

val libs = Seq(
  "com.google.code.findbugs" % "jsr305" % "3.0.2" % "compile"
) ++ netty ++ logback

val specs2 = Seq("org.specs2" %% "specs2-core" % "4.10.0" % "test")

val playJson = Seq(
  "com.typesafe.play" %% "play-json" % "2.9.2"
)

// todo rename to dsl
val common = project.settings(opts)
  .settings(
    name := "dsl",
    libraryDependencies ++= netty.map(_ % "provided, test") ++ logback ++ specs2 ++ Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided, test",
      "org.scala-lang" % "scala-compiler" % scalaVersion.value % "test"
    ),
    crossScalaVersions := supportedVersions
  )

val swagger = project.in(file("swagger"))
  .settings(opts)
  .settings(
    name := "omhs-swagger",
    libraryDependencies ++= playJson ++ netty, // todo rm
    crossScalaVersions := supportedVersions
  ).dependsOn(common)

val playJsonSupport = Project("play-json-support", file("play-json-support"))
  .settings(opts)
  .settings(
    name := "omhs-play-support",
    libraryDependencies ++= playJson.map(_ % "provided"),
    crossScalaVersions := supportedVersions
  ).dependsOn(common)

lazy val mainProject = Project("omhs", file("."))
  .settings(
    opts,
    libraryDependencies ++= libs ++ specs2 ++ playJson ++ Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value % "test"
    ),
    crossScalaVersions := Nil,
    publish / skip := true
  )
  .dependsOn(common, playJsonSupport, swagger)
  .aggregate(common, playJsonSupport, swagger)
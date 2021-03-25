
val scala12 = "2.12.13"
val scala13 = "2.13.5"
val supportedVersions = Seq(scala12, scala13)

ThisBuild / version := "0.0.2"
scalaVersion := scala12

ThisBuild / organization := "com.github.fntz"
ThisBuild / homepage := Some(url("https://github.com/fntz/omhs"))
ThisBuild / scmInfo := Some(ScmInfo(url("https://github.com/fntz/omhs"), "git@github.com:fntz/omhs.git"))
ThisBuild / developers := List(Developer("mike", "mike", "mike.fch1@gmail.com", url("https://github.com/fntz")))
ThisBuild / licenses += ("MIT", url("https://github.com/fntz/omhs/blob/master/LICENSE"))

sonatypeRepository := "https://s01.oss.sonatype.org/service/local"
sonatypeCredentialHost := "s01.oss.sonatype.org"

publishMavenStyle := true

ThisBuild / publishTo := {
  val nexus = "https://s01.oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots/")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

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
    "-Ydelambdafy:inline" // https://github.com/scala/bug/issues/10554
  ),
  scalacOptions in Test ++= Seq("-Yrangepos")
)

val nettyVersion = "4.1.60.Final"

val netty = Seq(
  "io.netty" % "netty-codec-http" % nettyVersion,
  "io.netty" % "netty-codec-http2" % nettyVersion
)

val slf4j = Seq("org.slf4j" % "slf4j-api" % "1.7.30")
val logback = Seq("ch.qos.logback"  %  "logback-classic"    % "1.2.3")

val libs = Seq(
  "com.google.code.findbugs" % "jsr305" % "3.0.2" % "compile"
) ++ netty ++ logback

val specs2 = Seq("org.specs2" %% "specs2-core" % "4.10.0" % "test")

val playJson = Seq(
  "com.typesafe.play" %% "play-json" % "2.9.2"
)

val circe = Seq(
  "io.circe" %% "circe-core" % "0.13.0",
  "io.circe" %% "circe-parser" % "0.13.0"
)

lazy val jsonlibs = playJson ++ circe

val dsl = project.settings(opts)
  .settings(
    name := "omhs-dsl",
    libraryDependencies ++= netty.map(_ % "provided, test") ++ slf4j.map(_ % "provided")
      ++ specs2 ++ Seq(
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
    crossScalaVersions := supportedVersions,
    publish / skip := true
  ).dependsOn(dsl)

val playJsonSupport = Project("play-json-support", file("play-json-support"))
  .settings(opts)
  .settings(
    name := "omhs-play-support",
    libraryDependencies ++= playJson.map(_ % "provided"),
    crossScalaVersions := supportedVersions
  ).dependsOn(dsl)

val circeSupport = Project("circe-support", file("circe-support"))
  .settings(opts)
  .settings(
    name := "omhs-circe-support",
    libraryDependencies ++= circe.map(_ % "provided"),
    crossScalaVersions := supportedVersions
  ).dependsOn(dsl)

lazy val mainProject = Project("omhs", file("."))
  .settings(
    opts,
    libraryDependencies ++= libs ++ slf4j ++ logback ++ specs2 ++ jsonlibs ++ Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value % "test"
    ),
    publish / skip := true
  )
  .dependsOn(dsl, playJsonSupport, circeSupport, swagger)
  .aggregate(dsl, playJsonSupport, circeSupport, swagger)
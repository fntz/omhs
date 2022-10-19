
val scala12 = "2.12.17"
val scala13 = "2.13.10"
val supportedVersions = Seq(scala12, scala13)

ThisBuild / version := "0.0.6"
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
  Test / scalacOptions ++= Seq("-Yrangepos"),
  testFrameworks += new TestFramework("munit.Framework")
)

val nettyVersion = "4.1.84.Final"

val netty = Seq(
  "io.netty" % "netty-codec-http" % nettyVersion,
  "io.netty" % "netty-codec-http2" % nettyVersion
)

val slf4j = Seq("org.slf4j" % "slf4j-api" % "2.0.3")
val logback = Seq("ch.qos.logback"  %  "logback-classic" % "1.4.4")

val libs = Seq(
  "com.google.code.findbugs" % "jsr305" % "3.0.2" % "compile"
) ++ netty ++ logback

val munit = Seq("org.scalameta" %% "munit" % "0.7.29" % "test")

val playJson = Seq(
  "com.typesafe.play" %% "play-json" % "2.9.3"
)

val circe = Seq(
  "io.circe" %% "circe-core" % "0.14.3",
  "io.circe" %% "circe-parser" % "0.14.3"
)

val jsoniterScala = Seq(
  "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % "2.17.5",
  "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % "2.17.5"
)

lazy val jsonLibs = playJson ++ circe ++ jsoniterScala

val dsl = project.settings(opts)
  .settings(
    name := "omhs-dsl",
    libraryDependencies ++= netty.map(_ % "provided") ++ slf4j.map(_ % "provided")
      ++ munit ++ Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value % "compile",
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
  ).dependsOn(dsl % "provided")

val circeSupport = Project("circe-support", file("circe-support"))
  .settings(opts)
  .settings(
    name := "omhs-circe-support",
    libraryDependencies ++= circe.map(_ % "provided"),
    crossScalaVersions := supportedVersions
  ).dependsOn(dsl % "provided")

val jsoniterSupport = Project("jsoniter-support", file("jsoniter-support"))
  .settings(opts)
  .settings(
    name := "omhs-jsoniter-support",
    libraryDependencies ++= jsoniterScala.map(_ % "provided") ++ netty.map(_ % "provided"),
    crossScalaVersions := supportedVersions
  ).dependsOn(dsl % "provided")

lazy val mainProject = Project("omhs", file("."))
  .settings(
    opts,
    libraryDependencies ++= libs ++ slf4j ++ logback ++ munit ++ jsonLibs ++ Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value % "test",
      "org.scala-lang.modules" %% "scala-collection-compat" % "2.8.1" % "test"
    ),
    publish / skip := true
  )
  .dependsOn(dsl, playJsonSupport, circeSupport, jsoniterSupport, swagger)
  .aggregate(dsl, playJsonSupport, circeSupport, jsoniterSupport, swagger)
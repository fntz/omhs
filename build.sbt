
val scala213 = "2.13.18"
val scala3   = "3.7.1"
val supportedVersions = Seq(scala213, scala3)

ThisBuild / version := "0.1.0"
ThisBuild / scalaVersion := scala3
ThisBuild / crossScalaVersions := supportedVersions

ThisBuild / organization := "com.github.fntz"
ThisBuild / homepage := Some(url("https://github.com/fntz/omhs"))
ThisBuild / scmInfo := Some(ScmInfo(url("https://github.com/fntz/omhs"), "git@github.com:fntz/omhs.git"))
ThisBuild / developers := List(Developer("mike", "mike", "mike.fch1@gmail.com", url("https://github.com/fntz")))
ThisBuild / licenses += ("MIT", url("https://github.com/fntz/omhs/blob/master/LICENSE"))

sonatypeRepository := "https://s01.oss.sonatype.org/service/local"
sonatypeCredentialHost := "s01.oss.sonatype.org"

publishMavenStyle := true

ThisBuild / publishTo := {
  val centralSnapshots = "https://central.sonatype.com/repository/maven-snapshots/"
  if (isSnapshot.value) Some("central-snapshots" at centralSnapshots)
  else localStaging.value
}

def commonScalacOptions(scalaV: String): Seq[String] = {
  val shared = Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-unchecked"
  )
  CrossVersion.partialVersion(scalaV) match {
    case Some((2, _)) =>
      shared ++ Seq(
        "-language:higherKinds",
        "-language:implicitConversions",
        "-language:postfixOps",
        "-language:reflectiveCalls",
        "-Xverify",
        "-Ydelambdafy:inline" // https://github.com/scala/bug/issues/10554
      )
    case Some((3, _)) =>
      shared ++ Seq(
        "-language:implicitConversions",
        "-language:postfixOps"
      )
    case _ => shared
  }
}

val opts = Seq(
  scalacOptions ++= commonScalacOptions(scalaVersion.value),
  Test / scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, _)) => Seq("-Yrangepos")
      case _            => Nil
    }
  },
  testFrameworks += new TestFramework("munit.Framework")
)

val nettyVersion = "4.1.137.Final"

val netty = Seq(
  "io.netty" % "netty-codec-http" % nettyVersion,
  "io.netty" % "netty-codec-http2" % nettyVersion
)

val slf4j = Seq("org.slf4j" % "slf4j-api" % "2.0.3")
val logback = Seq("ch.qos.logback"  %  "logback-classic" % "1.4.4")

val libs = Seq(
  "com.google.code.findbugs" % "jsr305" % "3.0.2" % "compile"
) ++ netty ++ logback

val munit = Seq("org.scalameta" %% "munit" % "1.1.0" % "test")

val playJson = Seq(
  "org.playframework" %% "play-json" % "3.0.6"
)

val circe = Seq(
  "io.circe" %% "circe-core" % "0.14.16",
  "io.circe" %% "circe-parser" % "0.14.16"
)

val jsoniterScala = Seq(
  "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % "2.40.1",
  "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % "2.40.1"
)

lazy val jsonLibs = playJson ++ circe ++ jsoniterScala

val dsl = project.settings(opts)
  .settings(
    name := "omhs-dsl",
    libraryDependencies ++= netty.map(_ % "provided") ++ slf4j.map(_ % "provided")
      ++ munit ++ {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 13)) =>
          Seq(
            "org.scala-lang" % "scala-reflect" % scalaVersion.value % "compile",
            "org.scala-lang" % "scala-compiler" % scalaVersion.value % "test"
          )
        case _ =>
          Seq.empty
      }
    },
    crossScalaVersions := supportedVersions
  )

// swagger stays unpublished / disabled for now
// val swagger = project.in(file("swagger"))
//   .settings(opts)
//   .settings(
//     name := "omhs-swagger",
//     libraryDependencies ++= playJson ++ netty, // todo rm
//     crossScalaVersions := supportedVersions,
//     publish / skip := true
//   ).dependsOn(dsl)

lazy val playJsonSupport = Project("play-json-support", file("play-json-support"))
  .settings(opts)
  .settings(
    name := "omhs-play-support",
    libraryDependencies ++= playJson.map(_ % "provided"),
    crossScalaVersions := supportedVersions
  ).dependsOn(dsl % "provided")

lazy val circeSupport = Project("circe-support", file("circe-support"))
  .settings(opts)
  .settings(
    name := "omhs-circe-support",
    libraryDependencies ++= circe.map(_ % "provided"),
    crossScalaVersions := supportedVersions
  ).dependsOn(dsl % "provided")

lazy val jsoniterSupport = Project("jsoniter-support", file("jsoniter-support"))
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
      "org.scala-lang.modules" %% "scala-collection-compat" % "2.8.1" % "test"
    ) ++ {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, _)) =>
          Seq("org.scala-lang" % "scala-reflect" % scalaVersion.value % "test")
        case _ =>
          Seq.empty
      }
    },
    publish / skip := true,
    crossScalaVersions := supportedVersions
  )
  .dependsOn(dsl, playJsonSupport, circeSupport, jsoniterSupport)
  .aggregate(dsl, playJsonSupport, circeSupport, jsoniterSupport)


scalaVersion := "2.12.8"

val opts = Seq(
  scalaVersion := "2.12.8",
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
    "-Xfuture"
  ),
  scalacOptions in Test ++= Seq("-Yrangepos"),
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8")
)

val libs = Seq(
  "com.google.code.findbugs" % "jsr305" % "3.0.2" % "compile",
  "io.netty" % "netty-codec-http" % "4.1.59.Final",
  "ch.qos.logback"  %  "logback-classic"    % "1.2.3"
)

val specs2 = Seq("org.specs2" %% "specs2-core" % "4.10.0" % "test")

val common = project.settings(opts)
  .settings(
    name := "common",
    libraryDependencies ++= specs2
  )

val macros = project.settings(opts)
  .settings(
    name := "macro",
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    ) ++ specs2
  ).dependsOn(common)

lazy val mainProject = Project("test", file("."))
  .settings(
    opts,
    libraryDependencies ++= libs ++ specs2 ++ Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value
    )
  )
  .dependsOn(common, macros)
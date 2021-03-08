
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
    "-Xfuture",
    "-Ydelambdafy:inline" // todo https://github.com/scala/bug/issues/10554
  ),
  scalacOptions in Test ++= Seq("-Yrangepos"),
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8")
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

val common = project.settings(opts)
  .settings(
    name := "common",
    libraryDependencies ++= specs2 ++ netty ++ logback ++ playJson
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
    libraryDependencies ++= libs ++ specs2 ++ playJson ++ Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "org.scala-lang" % "scala-compiler" % scalaVersion.value % "test"
    )
  )
  .dependsOn(common, macros)
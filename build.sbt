name := """p25"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  //jdbc,
  //cache,
  ws,
  "io.argonaut" %% "argonaut" % "6.1",
//  "org.reactivemongo" %% "reactivemongo" % "0.11.10",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.11.11",
  "com.pauldijou" %% "jwt-play" % "0.7.0",
//  "org.reactivemongo" %% "reactivemongo-play-json" % "0.11.11",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.0-RC1" % Test
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

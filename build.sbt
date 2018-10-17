import Libraries._


lazy val commonSettings = Seq(
    organization := "edu.pl.agh",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := "2.12.7",
    libraryDependencies ++= scalatest
)

lazy val root = (project in file("."))
  .settings(commonSettings,
    name := "AGH_Logistics"
  )
  .aggregate(
    http,
    transportTables
  )

lazy val http = (project in file("http"))
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      akka,
      circe
    ).flatten
  ).dependsOn(transportTables)

lazy val transportTables = (project in file("transport"))
.settings(commonSettings,
  libraryDependencies ++= Seq(
    breezze,
    circe
  ).flatten
)

resolvers ++= Seq(
  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Artima Maven Repository" at "http://repo.artima.com/releases"
)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
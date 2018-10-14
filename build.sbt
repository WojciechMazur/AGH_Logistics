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
      transportTables
  )

lazy val transportTables = (project in file("transport"))
.settings(commonSettings,
  libraryDependencies ++= breezze
)

resolvers ++= Seq(
  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Artima Maven Repository" at "http://repo.artima.com/releases"
)
import sbt._

object Libraries {
  object Versions {
    lazy val akka            = "2.5.17"
    lazy val akkaHttp        = "10.1.5"
    lazy val akkaHttpCirce   = "1.22.0"
    lazy val akkaHttpCors    = "0.3.1"
    lazy val breeze          = "0.13.2"
    lazy val circe           = "0.10.0"
    lazy val circeDerivation = "0.10.0-M1"
    lazy val scalatest       = "3.0.5"
  }

  lazy val breezze: Seq[ModuleID] = Seq(
    "org.scalanlp" %% "breeze" % Versions.breeze
//    "org.scalanlp" %% "breeze-natives" % Versions.breeze
  )

  lazy val circe: Seq[ModuleID] = Seq(
    "io.circe" %% "circe-core" % Versions.circe,
    "io.circe" %% "circe-generic" % Versions.circe,
    "io.circe" %% "circe-parser" % Versions.circe,
    "io.circe" %% "circe-derivation" % Versions.circeDerivation
  )

  lazy val akka: Seq[ModuleID] = Seq(
    "com.typesafe.akka" %% "akka-http" % Versions.akkaHttp,
    "com.typesafe.akka" %% "akka-stream" % Versions.akka,
    "ch.megard" %% "akka-http-cors" % Versions.akkaHttpCors,
    "de.heikoseeberger" %% "akka-http-circe" % Versions.akkaHttpCirce
  )

  lazy val scalatest: Seq[ModuleID] = Seq(
    "org.scalactic" %% "scalactic" % Versions.scalatest,
    "org.scalatest" %% "scalatest" % Versions.scalatest % "test"
  )

}

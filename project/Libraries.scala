import sbt._

object Libraries {
  object Versions {
    lazy val breeze = "0.13.2"
    lazy val scalatest = "3.0.5"
  }
  
  lazy val breezze: Seq[ModuleID] = Seq(
    // Last stable release
    "org.scalanlp" %% "breeze" % Versions.breeze,

    // Native libraries are not included by default. add this if you want them (as of 0.7)
    // Native libraries greatly improve performance, but increase jar sizes. 
    // It also packages various blas implementations, which have licenses that may or may not
    // be compatible with the Apache License. No GPL code, as best I know.
    "org.scalanlp" %% "breeze-natives" % Versions.breeze,

    // The visualization library is distributed separately as well.
    // It depends on LGPL code
    "org.scalanlp" %% "breeze-viz" % Versions.breeze
  )
  
  lazy val scalatest: Seq[ModuleID] = Seq(
    "org.scalactic" %% "scalactic" % Versions.scalatest,
    "org.scalatest" %% "scalatest" % Versions.scalatest % "test",
  )
  
}

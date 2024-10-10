name := "orderbook"
version := "1.0"
scalaVersion := "2.12.13"

// Chisel dependencies
libraryDependencies += "edu.berkeley.cs" %% "chisel3" % "3.5.4"
libraryDependencies += "edu.berkeley.cs" %% "chiseltest" % "0.5.4"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.9" % "test"

addCompilerPlugin("edu.berkeley.cs" % "chisel3-plugin" % "3.5.4" cross CrossVersion.full)

scalacOptions ++= Seq(
  "-Xsource:2.13",
)

fork in run := true
javaOptions in run += s"-Djava.library.path=${baseDirectory.value}/lib"

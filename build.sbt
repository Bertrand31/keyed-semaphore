val scala3Version = "3.0.0-RC3"

lazy val root = project
  .in(file("."))
  .settings(
    name := "scala3-simple",
    version := "0.1.0",

    scalaVersion := scala3Version,

    libraryDependencies += "org.typelevel" %% "cats-core" % "2.6.0",
    libraryDependencies += "org.typelevel" %% "cats-effect" % "3.1.0",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.8" % Test,

    Test / parallelExecution := false,
    concurrentRestrictions in Global += Tags.limit(Tags.Test, 1),
  )

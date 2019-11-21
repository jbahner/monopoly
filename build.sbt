name := "monopoly"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.5"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"
libraryDependencies ++= Seq(
    "junit" % "junit" % "4.11" % Test,
    "com.novocode" % "junit-interface" % "0.11" % Test
      exclude("junit", "junit-dep")
)
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.6"

coverageExcludedPackages := ".*view.*"
coverageExcludedFiles := ".*Monopoly"
name := "monopoly"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.5"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"
libraryDependencies += "org.scala-lang.modules" % "scala-swing_2.12" % "2.0.3"
libraryDependencies ++= Seq(
    "junit" % "junit" % "4.11" % Test,
    "com.novocode" % "junit-interface" % "0.11" % Test
      exclude("junit", "junit-dep")
)
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.6"
libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.1.1"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0"
libraryDependencies += "org.scala-lang.modules" % "scala-swing_2.12" % "2.0.3"
libraryDependencies += "com.google.inject" % "guice" % "4.1.0"
libraryDependencies += "net.codingwell" %% "scala-guice" % "4.1.0"

coverageExcludedPackages := ".*view.*"
coverageExcludedFiles := ".*Monopoly"
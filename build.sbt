

name := "monopoly"

version := "0.1"

scalaVersion := "2.12.8"


lazy val global = project
  .in(file("."))
  .aggregate(
      boardModule,
      playerModule,
      uiAndControllerModule,
      stateModule
  )

lazy val uiAndControllerModule = project
  .settings(name := "UiAndControllerModule",
      libraryDependencies ++= mainModuleDependencies)
  .dependsOn(boardModule, playerModule)

lazy val boardModule = project
  .settings(name := "BoardModule",
      libraryDependencies ++= mainModuleDependencies)
  .dependsOn(playerModule)
  .aggregate(playerModule)

lazy val playerModule = project
  .settings(name := "PlayerModule",
      libraryDependencies ++= mainModuleDependencies)
  .dependsOn(stateModule)
  .aggregate(stateModule)

lazy val stateModule = project
  .settings(name := "StateModule",
      libraryDependencies ++= mainModuleDependencies)


lazy val dependencies =
    new {
        val logbackV = "1.2.3"
        val logstashV = "4.11"
        val scalaLoggingV = "3.9.0"
        val slf4jV = "1.7.25"
        val typesafeConfigV = "1.3.1"
        val akkaV = "2.5.6"
        val scalatestV = "3.0.4"
        val scalacheckV = "1.13.5"

        val logback = "ch.qos.logback" % "logback-classic" % logbackV
        val logstash = "net.logstash.logback" % "logstash-logback-encoder" % logstashV
        val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingV
        val slf4j = "org.slf4j" % "jcl-over-slf4j" % slf4jV
        val typesafeConfig = "com.typesafe" % "config" % typesafeConfigV
        val akka = "com.typesafe.akka" %% "akka-stream" % akkaV
        val scalatest = "org.scalatest" %% "scalatest" % scalatestV
        val scalacheck = "org.scalacheck" %% "scalacheck" % scalacheckV
        val gguice = "com.google.inject" % "guice" % "4.1.0"
    }


lazy val mainModuleDependencies = Seq(
    dependencies.logback,
    dependencies.logstash,
    dependencies.scalaLogging,
    dependencies.slf4j,
    dependencies.typesafeConfig,
    dependencies.akka,
    dependencies.gguice,
    dependencies.scalatest % "test",
    dependencies.scalacheck % "test",
    "org.scalactic" %% "scalactic" % "3.0.5",
    "com.google.inject" % "guice" % "4.1.0",
    "net.codingwell" %% "scala-guice" % "4.1.0",
    "org.scala-lang.modules" % "scala-xml_2.12" % "1.1.1",
    "com.typesafe.play" %% "play-json" % "2.6.6",
    "org.scala-lang.modules" % "scala-swing_2.12" % "2.0.3",
    "org.scalafx" %% "scalafx" % "11-R16",
    "junit" % "junit" % "4.11" % Test,
    "com.novocode" % "junit-interface" % "0.11" % Test
      exclude("junit", "junit-dep"),
    "com.typesafe.akka" %% "akka-http" % "10.1.12",
    "com.typesafe.akka" %% "akka-stream" % "2.5.26" // or whatever the latest version is
)


coverageExcludedPackages := ".*view.*"
coverageExcludedFiles := ".*Monopoly"
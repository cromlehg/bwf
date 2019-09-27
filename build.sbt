name := """bwf-lightweight"""
organization := "com.blockwit"

PlayKeys.playDefaultPort := 9000

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

scalaVersion := "2.12.8"

// Needs to export unmanaged dependencies from lib folder
//exportJars := true

// To prevent binary incompatible warnings! TODO: Remove it if no more needs
/*val akkaVersion = "2.5.11"
dependencyOverrides ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.google.guava" % "guava" % "22.0"
)*/

val akkaVersion = "2.5.18"

libraryDependencies ++= Seq(
  guice,
  ws,
  "org.typelevel" %% "cats-core" % "1.2.0",
  "org.web3j" % "core" % "3.3.1",
  "com.github.t3hnar" %% "scala-bcrypt" % "3.1",
  "org.webjars.bower" % "jquery" % "3.3.1",
  "org.webjars" % "bootstrap" % "4.3.1",
  "com.adrianhurt" %% "play-bootstrap" % "1.5.1-P26-B4",
  "org.jsoup" % "jsoup" % "1.11.3",
  "com.typesafe.play" %% "play-slick" % "3.0.1",
  "mysql" % "mysql-connector-java" % "6.0.5",
  "net.sargue" % "mailgun" % "1.9.0",
  "org.ocpsoft.prettytime" % "prettytime" % "4.0.1.Final",
  "org.webjars" % "font-awesome" % "5.10.1",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.2.3",
  "com.github.tototoshi" %% "slick-joda-mapper" % "2.3.0",
  "org.flywaydb" %% "flyway-play" % "5.0.0",
  "be.objectify" %% "deadbolt-scala" % "2.6.1",
  "org.webjars" % "tinymce" % "4.7.9",
  "org.webjars" % "select2" % "4.0.5",
  "com.github.yanana" %% "uap-scala" % "0.1.4",

  "org.webjars.npm" % "prismjs" % "1.15.0",

  "org.webjars.bower" % "bootstrap-toggle" % "2.2.2",
  "org.webjars.bower" % "github-com-Nodws-bootstrap4-tagsinput" % "4.1.2",
	"org.webjars" % "select2" % "4.0.5",

"com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
"com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
"com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
"org.webjars" % "flot" % "0.8.3-1",

	"com.lightbend.play" %% "play-socket-io" % "1.0.0-beta-2",
	"org.webjars.bower" % "socket.io-client" % "2.0.3",



// Only for testing
  "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.0-M1" % Test,
  "com.h2database" % "h2" % "1.4.192" % Test
)

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
javaOptions in Test += "-Dconfig.file=conf/application.test.conf"

name := "EssentialSlick"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
   "com.typesafe.slick" %% "slick" % "3.1.0",
   "com.typesafe.slick" %% "slick-codegen" % "3.1.0",
   "ch.qos.logback" % "logback-classic" % "1.1.2",
   "mysql" % "mysql-connector-java" % "5.1.38"
)
    
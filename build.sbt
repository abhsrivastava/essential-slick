name := "EssentialSlick"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
   "com.typesafe.slick" %% "slick" % "3.1.0",
   "com.typesafe.slick" %% "slick-codegen" % "3.1.0",
   "mysql" % "mysql-connector-java" % "5.1.38"
)
    
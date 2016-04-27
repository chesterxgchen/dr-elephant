import sbt._
import sbt.Keys._

import play.Project._
import Dependencies._
import BuildConfig._


object ElephantBuild extends Build {

  val  projectName    = "dr-elephant"

  lazy val configs = Dependencies.configs

  lazy val projectVersion =configs.getProperty("project.version")
  lazy val debugOpts = configs.getProperty("java.compile.debug.option")
  lazy val sourceVersion = configs.getProperty("java.source.version")
  lazy val targetVersion = configs.getProperty("java.target.version")
  lazy val docSourceVersion =  configs.getProperty("java.doc.source.version")

  println("sourceVersion = " + sourceVersion)
  println("targetVersion = " + targetVersion)

  lazy val root = Project(
    id = "root",
    base = file("."),
    settings = Defaults.defaultSettings ++ Seq(
      name :=projectName,
      version :=projectVersion,
      libraryDependencies ++= dependencies  ,
      javacOptions in compile ++= Seq(debugOpts, "-source", sourceVersion, "-target", targetVersion),
      javacOptions in doc ++= Seq("-source", docSourceVersion),
      testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v")
    ) ++ playJavaSettings  ++ Seq(
          resolvers += "Cloudera repository" at "https://repository.cloudera.com/cloudera/cloudera-repos/",
          resolvers += "Hortonworks repository" at "http://repo.hortonworks.com/content/repositories/releases/",
          resolvers += "Maven repository" at "http://repo1.maven.org/maven2/"
        )

  )


}
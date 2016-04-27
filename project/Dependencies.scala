//
// Copyright 2016 LinkedIn Corp.
//
// Licensed under the Apache License, Version 2.0 (the "License"); you may not
// use this file except in compliance with the License. You may obtain a copy of
// the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
// License for the specific language governing permissions and limitations under
// the License.
//

import play.Project._
import sbt._
import BuildConfig._

object Dependencies {

  // Dependency Version
<<<<<<< 2d86f06571a2c6dc4d2c2bab9bbb8e3c3239f4d6
  lazy val commonsCodecVersion = "1.10"
  lazy val commonsIoVersion = "2.4"
  lazy val gsonVersion = "2.2.4"
  lazy val guavaVersion = "18.0"          // Hadoop defaultly are using guava 11.0, might raise NoSuchMethodException
=======
  lazy val commonsIoVersion        = "2.4"
  lazy val gsonVersion             = "2.2.4"
  lazy val guavaVersion            = "18.0"          // Hadoop defaultly are using guava 11.0, might raise NoSuchMethodException
>>>>>>> The Main changes are the followings:
  lazy val jacksonMapperAslVersion = "1.7.3"
  lazy val jsoupVersion            = "1.7.3"
  lazy val mysqlConnectorVersion   = "5.1.36"
  lazy val mockitoVersion          = "1.10.19"
  lazy val HADOOP_VERSION          = "hadoopversion"
  lazy val SPARK_VERSION           = "sparkversion"

  lazy val configs                 = loadConfigurations
  lazy val hadoopVersion           = getHadoopVersion
  lazy val sparkVersion            = getSparkVersion
  lazy val dbVendor                = configs.getProperty("db_vendor")

  // Dependency coordinates
  var requiredDep = commonDependencies ++
                    dbDependencies(dbVendor) ++
                    hadoopDependencies(hadoopVersion) ++
                    sparkDependencies(sparkVersion) ++
                    otherDependencies

  val dependencies = Seq(javaJdbc, javaEbean, cache) ++ requiredDep

  private def commonDependencies = Seq(
    "com.google.code.gson" % "gson" % gsonVersion,
    "com.google.guava" % "guava" % guavaVersion,
    "commons-io" % "commons-io" % commonsIoVersion
  )


  private def otherDependencies = Seq(
    "org.codehaus.jackson" % "jackson-mapper-asl" % jacksonMapperAslVersion,
    "org.jsoup" % "jsoup" % jsoupVersion,
    "org.mockito" % "mockito-core" % mockitoVersion
  )
  private def dbDependencies (dbVendor: String) = {
    dbVendor match  {
      case "mysql" =>
       Seq("mysql" % "mysql-connector-java" % mysqlConnectorVersion)
      case "postgresql" =>
        Seq("postgresql" % "postgresql" % "9.1-901.jdbc4")
      case _ =>
        Seq("mysql" % "mysql-connector-java" % mysqlConnectorVersion)
    }
  }


  private def sparkDependencies(sparkVersion: String) = {

    val rules = if (sparkVersion >= "1.5.0") {
      Seq(
        ExclusionRule(organization = "com.typesafe.akka"),
        ExclusionRule(organization = "org.apache.avro"),
        ExclusionRule(organization = "org.apache.hadoop"),
        ExclusionRule(organization = "net.razorvine")
      )
    }
    else {
      Seq(
        ExclusionRule(organization = "org.apache.avro"),
        ExclusionRule(organization = "org.apache.hadoop"),
        ExclusionRule(organization = "net.razorvine")
      )
    }

    Seq(
      "org.apache.spark" % "spark-core_2.10" % sparkVersion excludeAll(rules:_*)
    )
  }

  private def hadoopDependencies(hadoopVersion: String) = {
        Seq(
          "org.apache.hadoop" % "hadoop-auth" % hadoopVersion,
          "org.apache.hadoop" % "hadoop-common" % hadoopVersion ,
          "org.apache.hadoop" % "hadoop-common" % hadoopVersion % Test,
          "org.apache.hadoop" % "hadoop-hdfs" % hadoopVersion
        )
  }

  private def getHadoopVersion: String = {

    val sysProp = System.getProperties.getProperty(HADOOP_VERSION)
    if (sysProp != null) sysProp
    else {
      val envProp = System.getenv("HADOOP_VERSION")
      if (envProp != null) envProp
      else
        configs.getProperty("hadoop_version")
    }
  }


  private def getSparkVersion: String = {

    val sysProp = System.getProperties.getProperty(SPARK_VERSION)
    if (sysProp != null) sysProp
    else {
      val envProp = System.getenv("SPARK_VERSION")
      if (envProp != null) envProp
      else
        configs.getProperty("spark_version")
    }

  }
>>>>>>> The Main changes are the followings:

}

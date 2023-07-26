import sbt.url

scalaVersion := "2.13.11"

inThisBuild(
  List(
    organization := "io.cruxfinance",
    homepage := Some(url("https://cruxfinance.io")),
    licenses := List(License.MIT),
    developers := List(
      Developer(
        "luivatra",
        "Rob van Leeuwen",
        "luivatra@gmail.com",
        url("https://github.com/luivatra")
      )
    )
  )
)

name := "ergo-node-zmqpub"
organization := "io.cruxfinance"

resolvers ++= Seq(
  "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/",
  "Bintray" at "https://jcenter.bintray.com/", // for org.ethereum % leveldbjni-all
  "SonaType" at "https://oss.sonatype.org/content/groups/public",
  "Typesafe maven releases" at "https://dl.bintray.com/typesafe/maven-releases/",
  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
)

enablePlugins(DockerPlugin)
enablePlugins(JavaServerAppPackaging)

import com.typesafe.sbt.packager.docker.DockerChmodType
import com.typesafe.sbt.packager.docker.DockerPermissionStrategy
dockerChmodType := DockerChmodType.UserGroupWriteExecute
dockerPermissionStrategy := DockerPermissionStrategy.CopyChown
dockerUpdateLatest := true
dockerBaseImage := "openjdk:17"

libraryDependencies += "org.zeromq" % "jeromq" % "0.5.3"

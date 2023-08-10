package io.cruxfinance.types

import com.google.gson.GsonBuilder

import java.io.{FileWriter, Writer}
import scala.io.Source

case class Config(
    nodeURL: String,
    nodePeersPort: String,
    zmqIP: String,
    zmqPort: String
)

object Config {
  private val gson = new GsonBuilder().setPrettyPrinting().create()

  def read(filePath: String): Config = {
    val jsonString: String = Source.fromFile(filePath).mkString
    gson.fromJson(jsonString, classOf[Config])
  }

  def toJsonString(json: Config): String = {
    this.gson.toJson(json)
  }

  def write(filePath: String, newConfig: Config): Unit = {
    val writer: Writer = new FileWriter(filePath)
    writer.write(this.gson.toJson(newConfig))
    writer.close()
  }

}

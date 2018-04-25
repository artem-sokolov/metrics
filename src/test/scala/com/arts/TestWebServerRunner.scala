package com.arts

import com.arts.util.InMemoryPersister
import com.typesafe.config.ConfigFactory

object TestWebServerRunner {
  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.defaultApplication()
    val persister = new InMemoryPersister()
    WebServerRunner.run(config, persister)
  }
}

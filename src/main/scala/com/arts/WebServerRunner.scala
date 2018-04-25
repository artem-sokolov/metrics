package com.arts

import com.arts.persistence.{MapDbPersister, Persister}
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.duration._

object WebServerRunner {
  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.defaultApplication()
    val mapDbPersister = new MapDbPersister(config.getString("application.id"), config.getString("mapdb.path"))

    run(config, mapDbPersister)
  }

  def run(config: Config, persister: Persister): Unit = {
    val windowDuration = Duration.fromNanos(config.getDuration("window.duration").toNanos)
    new WebServer(windowDuration, persister).startServer("localhost", 8080)
  }
}

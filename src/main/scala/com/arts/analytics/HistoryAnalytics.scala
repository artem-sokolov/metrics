package com.arts.analytics

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import com.arts.data._
import com.arts.persistence.Persister

import scala.concurrent.Future

class HistoryAnalytics(persister: Persister) extends Actor with ActorLogging {
  import context._

  override def postStop(): Unit = persister.close()

  override def receive: Receive = {
    case TimedStats(timestamp, stats) =>
      persister.addStats(timestamp, stats)
    case Get(timestamp) =>
      val statsFuture: Future[Stats] = for {
        option <- persister.load(timestamp)
      } yield option.getOrElse(Stats.empty())
      statsFuture.pipeTo(sender)
    case Post(timestamp, username, reaction) =>
      persister.addReaction(timestamp, username, reaction)
  }
}

object HistoryAnalytics {
  def props(persister: Persister): Props = Props(new HistoryAnalytics(persister))
}
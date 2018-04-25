package com.arts.analytics

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import com.arts.data._

import scala.concurrent.duration.{FiniteDuration, _}
import scala.language.postfixOps

class RealtimeAnalytics(windowDuration: FiniteDuration, historyAnalytics: ActorRef, currentTimestamp: () => Long)
  extends Actor with ActorLogging {

  import context._

  private def normalize(timestamp: Long): Long = timestamp - (timestamp % windowDuration.toMillis)
  private def currentWindow(): Long = normalize(currentTimestamp())

  private implicit val historyAnalyticsTimeout: Timeout = Timeout(3 seconds)

  def receive = active(TimedStats(currentWindow(), Stats.empty()))

  def active: TimedStats => Receive = {
    case TimedStats(window, stats) if window == currentWindow() => {
      case message: Message =>
        val normalizedTimestamp = normalize(message.timestamp)
        if (normalizedTimestamp == window) { // working with current window
          message match {
            case Get(_) =>
              sender ! stats
            case Post(_, username, reaction) =>
              val newStats = stats.updateWithReaction(username, reaction)
              context become active(TimedStats(window, newStats))
          }
        } else { // working with window in the past
          message match {
            case Get(_) =>
              (historyAnalytics ? Get(normalizedTimestamp)).pipeTo(sender)
            case post: Post =>
              historyAnalytics ! post.copy(timestamp = normalizedTimestamp)
          }
        }
    }
    case TimedStats(window, stats) => {
      case message =>
        historyAnalytics ! TimedStats(window, stats)
        context become active(TimedStats(currentWindow(), Stats.empty()))
        self ! message
    }
  }
}

object RealtimeAnalytics {
  def props(windowDuration: FiniteDuration, historyAnalytics: ActorRef, currentTimestamp: () => Long): Props =
    Props(new RealtimeAnalytics(windowDuration, historyAnalytics, currentTimestamp))
}
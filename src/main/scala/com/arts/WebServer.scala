package com.arts

import java.time.Instant

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{HttpApp, Route}
import akka.pattern.ask
import akka.util.Timeout
import com.arts.analytics.{HistoryAnalytics, RealtimeAnalytics}
import com.arts.data._
import com.arts.persistence.Persister

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Try

class WebServer(windowDuration: FiniteDuration, persister: Persister) extends HttpApp {
  private lazy val historyAnalytics = systemReference.get().actorOf(HistoryAnalytics.props(persister), "history-analytics")
  private lazy val realtimeAnalytics = systemReference.get().actorOf(RealtimeAnalytics.props(windowDuration, historyAnalytics, now), "realtime-analytics")

  private def now(): Long = Instant.now().toEpochMilli

  override protected def routes: Route =
    path("analytics") {
      post {
        parameters("timestamp" ? now(), "username".as[String], "click".?, "impression".?) { (timestamp, username, click, impression) =>
          complete {
            if (click.isDefined) {
              realtimeAnalytics ! Post(timestamp, username, Click)
            } else if (impression.isDefined) {
              realtimeAnalytics ! Post(timestamp, username, Impression)
            }
            StatusCodes.OK
          }
        }
      } ~
        get {
          parameters("timestamp" ? now()) { (timestamp) =>
            implicit val timeout: Timeout = Timeout(1 second)
            val stats = (realtimeAnalytics ? Get(timestamp)).map(_.toString)
            onSuccess(stats) { stats =>
              complete((StatusCodes.OK, stats))
            }
          }
        }
    }

  override def postServerShutdown(attempt: Try[Done], system: ActorSystem): Unit = {
    systemReference.get().log.info("terminating...")
    system.terminate()
    super.postServerShutdown(attempt, system)
  }
}


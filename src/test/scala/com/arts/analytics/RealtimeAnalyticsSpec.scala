package com.arts.analytics

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestActors, TestKit, TestProbe}
import com.arts.data._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import scala.language.postfixOps

class RealtimeAnalyticsSpec extends TestKit(ActorSystem("MySpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  private val anHourWindow = 1 hours

  def realtimeAnalyticsNoHistory(): ActorRef =
    system.actorOf(RealtimeAnalytics.props(anHourWindow, system.actorOf(TestActors.blackholeProps), () => 0))

  "RealtimeAnalytics actor" must {
    "be empty after initialization" in {
      val realtimeAnalytics = realtimeAnalyticsNoHistory()
      realtimeAnalytics ! Get(0)
      expectMsg(Stats(Set.empty[String], 0, 0))
    }
  }

  "RealtimeAnalytics actor" must {
    "correctly maintain number of clicks count in current window" in {
      val realtimeAnalytics = realtimeAnalyticsNoHistory()
      realtimeAnalytics ! Post(0, "Bob", Click)
      realtimeAnalytics ! Get(0)
      expectMsg(Stats(Set("Bob"), 1, 0))
      realtimeAnalytics ! Post(0, "Bob", Click)
      realtimeAnalytics ! Get(0)
      expectMsg(Stats(Set("Bob"), 2, 0))
    }
  }

  "RealtimeAnalytics actor" must {
    "correctly maintain number of impressions count in current window" in {
      val realtimeAnalytics = realtimeAnalyticsNoHistory()
      realtimeAnalytics ! Post(0, "Bob", Impression)
      realtimeAnalytics ! Get(0)
      expectMsg(Stats(Set("Bob"), 0, 1))
      realtimeAnalytics ! Post(0, "Bob", Impression)
      realtimeAnalytics ! Get(0)
      expectMsg(Stats(Set("Bob"), 0, 2))
    }
  }

  "RealtimeAnalytics actor" must {
    "correctly store unique users" in {
      val realtimeAnalytics = realtimeAnalyticsNoHistory()
      realtimeAnalytics ! Post(0, "Bob", Impression)
      realtimeAnalytics ! Post(0, "Bob", Impression)
      realtimeAnalytics ! Post(0, "Alice", Impression)
      realtimeAnalytics ! Get(0)
      expectMsg(Stats(Set("Bob", "Alice"), 0, 3))
    }
  }

  "RealtimeAnalytics actor" must {
    "send stats to history actor when window rolls" in {
      val historyAnalytics = TestProbe()

      var callCount = 0
      val mockTimestamp = () => { callCount += 1; if (callCount > 3) anHourWindow.toMillis + 1 else 0L }

      val realtimeAnalytics = system.actorOf(RealtimeAnalytics.props(anHourWindow, historyAnalytics.ref, mockTimestamp))

      realtimeAnalytics ! Post(0, "Alice", Impression)
      realtimeAnalytics ! Post(0, "Bob", Click)
      realtimeAnalytics ! Post(0, "Alice", Click)

      historyAnalytics.expectMsg(TimedStats(0, Stats(Set("Bob", "Alice"), 1, 1)))
    }
  }
}
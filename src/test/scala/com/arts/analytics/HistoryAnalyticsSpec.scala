package com.arts.analytics

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestActorRef, TestActors, TestKit, TestProbe}
import com.arts.data._
import com.arts.util.InMemoryPersister
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.language.postfixOps

class HistoryAnalyticsSpec extends TestKit(ActorSystem("MySpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  def historyAnalyticsMemoryPersister(): (InMemoryPersister, ActorRef) = {
    val persister = new InMemoryPersister()
    (persister, system.actorOf(HistoryAnalytics.props(persister)))
  }

  "HistoryAnalytics actor" must {
    "close persister when stopped" in {
      val (persister, historyAnalytics) = historyAnalyticsMemoryPersister()
      assert(!persister.closed)
      val watcher = TestProbe()
      watcher.watch(historyAnalytics)
      system.stop(historyAnalytics)
      watcher.expectTerminated(historyAnalytics)
      assert(persister.closed)
    }
  }

  "HistoryAnalytics actor" must {
    "return empty stats for nonexistent timestamp" in {
      val historyAnalytics = historyAnalyticsMemoryPersister()._2
      historyAnalytics ! Get(4)
      expectMsg(Stats.empty())
    }
  }

  "HistoryAnalytics actor" must {
    "be able to save new stats" in {
      val historyAnalytics = historyAnalyticsMemoryPersister()._2
      val stats = Stats(Set("Bob"), 1, 2)
      historyAnalytics ! TimedStats(4, stats)
      historyAnalytics ! Get(4)
      expectMsg(stats)
    }
  }

  "HistoryAnalytics actor" must {
    "be able to update stats" in {
      val historyAnalytics = historyAnalyticsMemoryPersister()._2
      val stats = Stats(Set("Bob"), 1, 2)
      historyAnalytics ! TimedStats(4, stats)
      historyAnalytics ! TimedStats(8, stats)
      historyAnalytics ! Post(4, "Alice", Impression)
      historyAnalytics ! Get(4)
      expectMsg(Stats(Set("Bob", "Alice"), 1, 3))
      historyAnalytics ! Get(8)
      expectMsg(stats)
    }
  }
}
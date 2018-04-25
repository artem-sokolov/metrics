package com.arts.data

import scala.collection.immutable.HashSet

case class Stats(users: Set[String], clicksCount: Long, impressionsCount: Long) {
  override def toString: String =
    s"""
       |unique_users,${users.size}
       |clicks,$clicksCount
       |impressions,$impressionsCount
     """.stripMargin.trim

  def updateWithReaction(username: String, reaction: Reaction): Stats = {
    val newUsers = users + username
    reaction match {
      case Click => this.copy(users = newUsers, clicksCount = clicksCount + 1)
      case Impression => this.copy(users = newUsers, impressionsCount = impressionsCount + 1)
    }
  }
}

object Stats {
  def empty(): Stats = Stats(HashSet.empty, 0, 0)
}

case class TimedStats(timestamp: Long, stats: Stats)
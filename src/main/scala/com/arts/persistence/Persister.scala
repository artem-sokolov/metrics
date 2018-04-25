package com.arts.persistence

import com.arts.data.{Reaction, Stats}

import scala.concurrent.Future

// it's responsibility of the persister implementation to make these calls thread safe
trait Persister {
  def close(): Unit

  def load(timestamp: Long): Future[Option[Stats]]

  def addStats(timestamp: Long, stats: Stats): Future[Unit]

  def addReaction(timestamp: Long, username: String, reaction: Reaction): Future[Unit]
}
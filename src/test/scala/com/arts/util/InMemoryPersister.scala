package com.arts.util

import java.util.concurrent.locks.ReentrantReadWriteLock

import com.arts.data.{Reaction, Stats}
import com.arts.persistence.Persister

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class InMemoryPersister extends Persister {
  private val map = new mutable.HashMap[Long, Stats]
  private val readWriteLock =  new ReentrantReadWriteLock()

  var closed: Boolean = false

  override def close(): Unit = closed = true

  override def load(timestamp: Long): Future[Option[Stats]] = Future {
    readWriteLock.readLock().lock()
    val stats = map.get(timestamp)
    readWriteLock.readLock().unlock()
    stats
  }

  override def addStats(timestamp:Long, stats: Stats): Future[Unit] = Future {
    readWriteLock.writeLock().lock()
    map.put(timestamp, stats)
    readWriteLock.writeLock().unlock()
  }

  override def addReaction(timestamp: Long, username: String, reaction: Reaction): Future[Unit] = Future {
    readWriteLock.writeLock().lock()
    val stats = map.getOrElse(timestamp, Stats.empty())
    map.put(timestamp, stats.updateWithReaction(username, reaction))
    readWriteLock.writeLock().unlock()
  }
}
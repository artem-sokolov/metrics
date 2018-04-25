package com.arts.persistence

import java.io.File
import java.nio.file.{Files, Paths}

import com.arts.data.{Reaction, Stats}
import org.mapdb.{DBMaker, HTreeMap}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MapDbPersister(applicationId: String, path: String) extends Persister {
  private val db = DBMaker
    .fileDB(new File(Files.createDirectories(Paths.get(path)).toFile, applicationId))
    .fileMmapEnableIfSupported()
    .make()
  private val map = db.hashMap(applicationId).createOrOpen().asInstanceOf[HTreeMap[Long, Stats]]

  override def close(): Unit = db.close()

  override def load(timestamp: Long): Future[Option[Stats]] = Future {
    if (map.containsKey(timestamp)) {
      Some(map.get(timestamp))
    } else {
      None
    }
  }

  override def addStats(timestamp:Long, stats: Stats): Future[Unit] = Future {
    map.put(timestamp, stats)
  }

  override def addReaction(timestamp: Long, username: String, reaction: Reaction): Future[Unit] = Future {
    if (!map.containsKey(timestamp)) {
      map.putIfAbsent(timestamp, Stats.empty())
    }
    map.transformValue(timestamp, _.updateWithReaction(username, reaction))
  }
}
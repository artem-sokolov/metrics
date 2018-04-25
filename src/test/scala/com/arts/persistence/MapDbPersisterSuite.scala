package com.arts.persistence

import java.io.File
import java.util.UUID

import com.arts.data.{Impression, Stats}
import org.scalatest.{AsyncFunSuite, Matchers}

class MapDbPersisterSuite extends AsyncFunSuite with Matchers {
  private val tempFolder = System.getProperty("java.io.tmpdir")

  test("should persist data") {
    val applicationId = UUID.randomUUID().toString
    new File(tempFolder, applicationId).delete()

    val persister = new MapDbPersister(applicationId, tempFolder)
    val stats = Stats(Set("Alice"), 3, 4)
    persister.addStats(0, stats).flatMap { _ =>
      persister.close()
      val anotherPersister = new MapDbPersister("test-1", System.getProperty("java.io.tmpdir"))
      anotherPersister.load(0).map(_.shouldBe(Some(stats)))
    }
  }

  test("should return None when key is not present") {
    val applicationId = UUID.randomUUID().toString
    val persister = new MapDbPersister(applicationId, tempFolder)
    persister.load(123).map(_.shouldBe(None))
  }

  test("should return Stats after adding") {
    val applicationId = UUID.randomUUID().toString
    val persister = new MapDbPersister(applicationId, tempFolder)
    val stats = Stats(Set("Alice"), 3, 4)

    persister.addStats(0, stats).flatMap { _ =>
      persister.load(0).map(_.shouldBe(Some(stats)))
    }
  }

  test("should be able to update Stats") {
    val applicationId = UUID.randomUUID().toString
    val persister = new MapDbPersister(applicationId, tempFolder)
    val stats = Stats(Set("Alice"), 3, 4)

    persister.addStats(0, stats).flatMap { _ =>
      persister.addReaction(0, "Bob", Impression).flatMap { _ =>
        persister.load(0).map(_.shouldBe(Some(Stats(Set("Alice", "Bob"), 3, 5))))
      }
    }
  }
}

package com.arts.data

import org.scalatest.FunSuite

class StatsSuite extends FunSuite {
  test("update stats with a click") {
    val stats = Stats.empty()
    val updated0 = stats.updateWithReaction("Alice", Click)
    assert(Stats(Set("Alice"), 1, 0) === updated0)
    val updated1 = updated0.updateWithReaction("Alice", Click)
    assert(Stats(Set("Alice"), 2, 0) === updated1)
  }

  test("update stats with an impression") {
    val stats = Stats.empty()
    val updated0 = stats.updateWithReaction("Alice", Impression)
    assert(Stats(Set("Alice"), 0, 1) === updated0)
    val updated1 = updated0.updateWithReaction("Alice", Impression)
    assert(Stats(Set("Alice"), 0, 2) === updated1)
  }

  test("store users as a set") {
    val stats = Stats.empty()
    val updated0 = stats.updateWithReaction("Alice", Impression)
    val updated1 = updated0.updateWithReaction("Bob", Click)
    assert(Stats(Set("Alice", "Bob"), 1, 1) === updated1)
  }
}

package com.arts

import org.mapdb.HTreeMap

import scala.annotation.tailrec

package object persistence {
  implicit class AtomicallyTransformableHTreeMap[K, V](hTreeMap: HTreeMap[K, V]) {
    @tailrec
    final def transformValue(key: K, function: V => V): Unit = {
      val oldValue = hTreeMap.get(key)
      val newValue = function(oldValue)
      if (!hTreeMap.replace(key, oldValue, newValue)) {
        transformValue(key, function)
      }
    }
  }
}

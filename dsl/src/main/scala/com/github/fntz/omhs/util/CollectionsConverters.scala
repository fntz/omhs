package com.github.fntz.omhs.util

import java.util
import scala.collection.mutable
import scala.collection.mutable.{Buffer => B, Map => mMap}

private [omhs] object CollectionsConverters {

  implicit class JMapExt[K, V](val jMap: java.util.Map[K, V]) extends AnyVal {
    def toScala: Map[K, V] = {
      if (jMap == null) {
        Map.empty[K, V]
      } else {
        val m = mMap[K, V]()
        jMap.entrySet().forEach { x => m += x.getKey -> x.getValue }
        m.toMap
      }
    }
  }

  implicit class JCollectionExt[T](val jCollection: java.util.Collection[T]) extends AnyVal {
    def toScala: Iterable[T] = {
      if (jCollection == null || jCollection.isEmpty) {
        Nil
      } else {
        val empty = B[T]()
        jCollection.forEach(x => empty += x)
        empty
      }
    }
  }

  implicit class SIterableExt[T](val sIterable: Iterable[T]) extends AnyVal {
    def toJava: java.lang.Iterable[T] = {
      val col = new util.ArrayList[T]()
      sIterable.foreach(col.add)
      col
    }
  }



}

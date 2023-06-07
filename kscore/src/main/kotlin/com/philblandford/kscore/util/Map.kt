package com.philblandford.kscore.util

import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.duration.plus
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf

fun <K,V>Iterable<Pair<K,V>>.toImmutableMap(): ImmutableMap<K, V> {
  val map = persistentMapOf<K, V>()
  return fold(map) { m, (k,v) ->
    m.put(k,v)
  }
}

inline fun <T> Iterable<T>.sumOf(selector: (T) -> Duration): Duration {
  var sum: Duration = dZero()
  for (element in this) {
    sum += selector(element)
  }
  return sum
}
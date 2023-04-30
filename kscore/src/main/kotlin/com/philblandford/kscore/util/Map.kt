package com.philblandford.kscore.util

import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf

fun <K,V>Iterable<Pair<K,V>>.toImmutableMap(): ImmutableMap<K, V> {
  val map = persistentMapOf<K, V>()
  return fold(map) { m, (k,v) ->
    m.put(k,v)
  }
}
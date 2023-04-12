package com.philblandford.kscore.engine.util

import java.util.*

fun <T> List<T>.removeAt(idx: Int): List<T> {
  val mutable = toMutableList()
  mutable.removeAt(idx)
  return mutable.toList()
}

fun <T> List<T>.add(idx: Int, element: T): List<T> {
  val mutable = toMutableList()
  mutable.add(idx, element)
  return mutable.toList()
}

fun <T> List<T>.replace(idx: Int, element: T): List<T> {
  val mutable = toMutableList()
  if (idx < size) {
    mutable.removeAt(idx)
  }
  mutable.add(idx, element)
  return mutable.toList()
}

fun <T> List<T>.replace(old: T, new: T): List<T> {
  val idx = indexOf(old)
  return if (idx >= 0) {
    replace(idx, new)
  } else {
    this
  }
}

fun <T> SortedMap<T, *>.lastKeyOrNull(): T? {
  try {
    return lastKey()
  } catch (e: Exception) {
    return null
  }
}

fun <T, S> MutableMap<T, S>.removeWhen(f: (T, S) -> Boolean) {
  forEach { (k, v) ->
    if (f(k, v)) {
      remove(k)
    }
  }
}

fun <T, S> SortedMap<T, S>.removeWhen(f: (T, S) -> Boolean) {
  val map = filterNot { f(it.key, it.value) }
  clear()
  putAll(map)
}

fun <T> MutableList<T>.removeWhen(f: (T) -> Boolean) {
  forEach { i ->
    if (f(i)) {
      remove(i)
    }
  }
}
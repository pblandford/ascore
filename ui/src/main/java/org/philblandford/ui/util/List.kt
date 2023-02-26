package org.philblandford.ui.util

fun <T>List<T>.reorder(fromIndex:Int, toIndex:Int):List<T> {
  return toMutableList().apply {
    add(toIndex, removeAt(fromIndex))
  }
}
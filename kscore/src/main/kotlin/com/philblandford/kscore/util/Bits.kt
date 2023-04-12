package com.philblandford.kscore.util

import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min

/**
 * Created by philb on 01/02/18.
 */



fun Int.highestBit(): Int {
  var cnt = 0
  var number = this
  while (number > 0) {
    number  = number shr  1
    cnt += 1
  }
  return cnt
}

fun Int.numOnes():Int {
  var cnt = 0
  var number = this
  while (number > 0) {
    if (number and 0x1 == 0x1) {
      cnt++
    }
    number = number shr 1
  }
  return cnt
}

fun Long.numBytes():Int {
  var numBytes = 4
  while (numBytes > 0) {
    if ((this shr ((numBytes-1L) * 8L).toInt()) and 0xff.toLong() != 0L) {
      return numBytes
    }
    numBytes -= 1
  }
  return 0
}

fun Int.isPower2():Boolean {
  var marker = 1
  while (marker < this) {
    if (marker and this != 0) {
      return false
    }
    marker = marker shl 1
  }
  return true
}

fun Int.lcm(other:Int):Int {
  return (this * other)/this.gcd(other)
}

fun Int.gcd(other:Int):Int {
  val greater = max(this, other)
  val lesser = min(this, other)
  return doGcd(greater, lesser)
}

fun String.toIntOrNull():Int? {
  return try {
    this.toInt()
  } catch (e:Exception) {
    null
  }
}

private fun doGcd(g:Int, l:Int):Int {
  val remainder = g%l
  if (remainder == 0) {
    return l
  }
  return doGcd(l, remainder)
}

fun Float.toDegrees():Float {
  return ((this * 180)/ PI).toFloat()
}

fun Float.toRadians():Float {
  return ((this * PI)/ 180).toFloat()
}
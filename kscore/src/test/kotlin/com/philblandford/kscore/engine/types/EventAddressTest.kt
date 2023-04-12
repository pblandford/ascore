package com.philblandford.kscore.engine.types

import com.philblandford.kscore.engine.duration.crotchet
import org.junit.Test

class EventAddressTest {

  @Test
  fun testEventAddressEqual() {
    val ea1 = ea(1)
    val ea2 = ea(1)
    assert(ea1 == ea2)
  }

  @Test
  fun testCompareBarsLessThan() {
    val ea1 = ea(1)
    val ea2 = ea(2)
    assert(ea1 < ea2)
  }

  @Test
  fun testCompareBarsMoreThan() {
    val ea1 = ea(1)
    val ea2 = ea(2)
    assert(ea2 > ea1)
  }

  @Test
  fun testCompareOffsetsLessThan() {
    val ea1 = ea(1)
    val ea2 = ea(1, crotchet())
    assert(ea1 < ea2)
  }

  @Test
  fun testCompareOffsetMoreThan() {
    val ea1 = ea(1)
    val ea2 = ea(1, crotchet())
    assert(ea2 > ea1)
  }

  @Test
  fun testCompareGraceOffsetLessThan() {
    val ea1 = eag(1)
    val ea2 = eag(1, graceOffset = crotchet())
    assert(ea1 < ea2)
  }

  @Test
  fun testCompareGraceOffsetLessThanNormal() {
    val ea1 = eag(1)
    val ea2 = ea(1)
    assert(ea1 < ea2)
  }
}
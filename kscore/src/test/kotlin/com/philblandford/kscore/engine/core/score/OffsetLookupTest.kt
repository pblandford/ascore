package com.philblandford.kscore.engine.core.score

import assertEqual
import com.philblandford.kscore.engine.types.ez
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.time.TimeSignature
import org.junit.Test

class OffsetLookupTest {

  @Test
  fun testAddDuration() {
    val ol = offsetLookup(tsMap, 2)
    assertEqual(ez(1, crotchet()), ol.addDuration(ez(1), crotchet()))
  }

  @Test
  fun testAddDurationPastBar() {
    val ol = offsetLookup(tsMap, 2)
    assertEqual(ez(2, minim()), ol.addDuration(ez(1, minim()), semibreve()))
  }

  @Test
  fun testAddDurationPastBarMinim() {
    val ol = offsetLookup(tsMap, 2)
    assertEqual(ez(2), ol.addDuration(ez(1, minim()), minim()))
  }

  @Test
  fun testAddDurationSemibreve() {
    val ol = offsetLookup(tsMap, 2)
    assertEqual(ez(2), ol.addDuration(ez(1), semibreve()))
  }

  @Test
  fun testAddDuration12_8Semibreve() {
    val ol = offsetLookup(mapOf(1 to TimeSignature(12, 8)), 2)
    assertEqual(ez(1, semibreve().add(quaver())), ol.addDuration(ez(1, semibreve()), quaver()))
  }

  @Test
  fun testAddressToOffset() {
    val ol = offsetLookup(tsMap, 2)
    assertEqual(Duration(1), ol.addressToOffset(ez(2)))
  }

  @Test
  fun testAddressToOffsetMidBar() {
    val ol = offsetLookup(tsMap, 2)
    assertEqual(Duration(3, 2), ol.addressToOffset(ez(2, minim())))
  }

  @Test
  fun testAddressToOffsetPastEnd() {
    val ol = offsetLookup(tsMap, 2)
    assertEqual(Duration(2), ol.addressToOffset(ez(3)))
  }

  @Test
  fun testOffsetToAddress() {
    val ol = offsetLookup(tsMap, 2)
    assertEqual(ez(2), ol.offsetToAddress(semibreve()))
  }

  @Test
  fun testOffsetToAddress3_4() {
    val ol = offsetLookup(mapOf(1 to TimeSignature(3,4)), 2)
    assertEqual(ez(2), ol.offsetToAddress(minim(1)))
  }

  @Test
  fun testOffsetToAddressEndBar() {
    val ol = offsetLookup(tsMap, 2)
    assertEqual(ez(2, minim(1)), ol.offsetToAddress(Offset(7,4)))
  }

  @Test
  fun testGetDuration() {
    val ol = offsetLookup(tsMap, 4)
    assertEqual(semibreve(), ol.getDuration(ez(1), ez(2)))
  }

  @Test
  fun testAddDurationChangeTs() {
    val ol = offsetLookup(tsMap.plus(3 to TimeSignature(2, 4)), 5)
    assertEqual(ez(3), ol.addDuration(ez(2, minim(1)), crotchet()))
  }

  @Test
  fun testAddDurationPastEnd() {
    val ol = offsetLookup(tsMap, 2)
    assertEqual(ez(4), ol.addDuration(ez(2), breve()))
  }

  @Test
  fun testOffsetLookupFromLastDuration() {
    val ol = offsetLookup(tsMap, Duration(2))
    assertEqual(Duration(1), ol.addressToOffset(ez(2)))
  }

  private val tsMap = mapOf(1 to TimeSignature(4, 4))
}
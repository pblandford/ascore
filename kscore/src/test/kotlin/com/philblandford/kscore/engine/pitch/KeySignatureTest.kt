package com.philblandford.kscore.engine.pitch

import assertEqual
import com.philblandford.kscore.engine.types.Accidental
import org.junit.Test
import com.philblandford.kscore.engine.scorefunction.ScoreTest

class KeySignatureTest : ScoreTest() {

  @Test
  fun testTransposeKey() {
    assertEqual(2, transposeKey(0, 2))
  }

  @Test
  fun testTransposeDown() {
    assertEqual(-2, transposeKey(0, -2))
  }

  @Test
  fun testTransposeKeyPreferFlat() {
    assertEqual(-6, transposeKey(-1, 1, Accidental.FLAT))
  }

  @Test
  fun testTransposeKeyPreferSharp() {
    assertEqual(6, transposeKey(-1, 1, Accidental.SHARP))
  }

  @Test
  fun testGetKeyDistance() {
    assertEqual(2, keyDistance(0,2))
  }

  @Test
  fun testGetKeyDistanceAcrossOctave() {
    assertEqual(2, keyDistance(-2,0))
  }

  @Test
  fun testGetKeyDistanceDown() {
    assertEqual(-2, keyDistance(4,2))
  }

  @Test
  fun testGetKeyDistanceSpecifyDown() {
    assertEqual(-10, keyDistance(0,2, false))
  }
}
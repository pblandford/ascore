package com.philblandford.kscore.engine.core.endtoend

import assertEqual
import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.types.*


import core.representation.*
import org.junit.Test

class KeyTest : RepTest() {

  @Test
  fun testAddKeyStartScore() {
    SAE(EventType.KEY_SIGNATURE, ez(1), paramMapOf(EventParam.SHARPS to 1))
    RVA("KeySignature", ea(1))
  }

  @Test
  fun testAddKeyStartScoreAccidentalsPlacedCorrectly() {
    SAE(EventType.KEY_SIGNATURE, ez(1), paramMapOf(EventParam.SHARPS to 1))
    val stave = getArea("Stave", ea(1))!!
    val accidental = getCentreOfAccidental(ea(1), 0)
    // F# on top line in treble
    assertEqual(stave.coord.y, accidental)
  }

  @Test
  fun testAddKeyStartScoreAccidentalsPlacedCorrectlyBassClef() {
    SAE(EventType.CLEF, ea(1), paramMapOf(EventParam.TYPE to ClefType.BASS))
    SAE(EventType.KEY_SIGNATURE, ez(1), paramMapOf(EventParam.SHARPS to 1))
    val stave = getArea("Stave", ea(1))!!
    val accidental = getCentreOfAccidental(ea(1), 0)
    // F# on second line in bass
    assertEqual(stave.coord.y + BLOCK_HEIGHT*2, accidental)
  }


  @Test
  fun testAddKeyMidScore() {
    SAE(EventType.KEY_SIGNATURE, ez(2), paramMapOf(EventParam.SHARPS to 2))
    RVA("KeySignature", ea(2))
  }

  @Test
  fun testAddKeyMidScoreCMajor() {
    SAE(EventType.KEY_SIGNATURE, ez(1), paramMapOf(EventParam.SHARPS to 2))
    SAE(EventType.KEY_SIGNATURE, ez(3), paramMapOf(EventParam.SHARPS to 0))
    val cancellations = getAreas("Accidental-♮")
    assertEqual(2, cancellations.size)
  }

  @Test
  fun testAddKeyBar2AfterBarLine() {
    SAE(EventType.KEY_SIGNATURE, ez(2), paramMapOf(EventParam.SHARPS to 2))
    assert(isLeft("BarLine", eas(1,1,0), "KeySignature", ea(2)) == true)
  }

  @Test
  fun testAddFiveFlats() {
    RCD(ks = -5)
    val ks = getArea("KeySignature", ea(1))!!
    val accidentals = ks.area.findByTag("Accidental-b")
    assertEqual(5, accidentals.size)

  }

  @Test
  fun testAddKSTransposing() {
    RCD(ks = 2, instruments = listOf("Trumpet"))
    val ks = getArea("KeySignature", ea(1))!!
    val accidentals = ks.area.findByTag("Accidental-#")
    assertEqual(4, accidentals.size)
  }

  private fun getCentreOfAccidental(eventAddress: EventAddress, num:Int): Int {
    val ks = getArea("KeySignature", eventAddress)!!
    val acc = ks.area.childMap.toList()[num]
    return ks.coord.y - acc.second.yMargin + BLOCK_HEIGHT*3
  }

}
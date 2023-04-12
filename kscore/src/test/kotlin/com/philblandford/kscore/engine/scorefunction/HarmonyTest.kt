package com.philblandford.kscore.engine.scorefunction


import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.types.NoteLetter.*
import com.philblandford.kscore.engine.types.Accidental.*
import com.philblandford.kscore.engine.duration.crotchet
import org.junit.Test

class HarmonyTest : ScoreTest() {

  @Test
  fun testAddHarmony() {
    SMV()
    SAE(EventType.HARMONY, ea(1), params = paramMapOf(EventParam.TEXT to "C"))
    SVP(EventType.HARMONY, EventParam.TONE, Pitch(C, NATURAL, 0), ea(1))
  }

  @Test
  fun testAddHarmonyQuality() {
    SMV()
    SAE(EventType.HARMONY, ea(1), params = paramMapOf(EventParam.TEXT to "C+"))
    SVP(EventType.HARMONY, EventParam.TONE, Pitch(C, NATURAL, 0), ea(1))
    SVP(EventType.HARMONY, EventParam.QUALITY, "+", ea(1))
  }


  @Test
  fun testAddHarmonyAccidentalQuality() {
    SMV()
    SAE(EventType.HARMONY, ea(1), params = paramMapOf(EventParam.TEXT to "C#7"))
    SVP(EventType.HARMONY, EventParam.TONE, Pitch(C, SHARP, 0), ea(1))
    SVP(EventType.HARMONY, EventParam.QUALITY, "7", ea(1))
  }

  @Test
  fun testAddHarmonyAccidentalQualityRoot() {
    SMV()
    SAE(EventType.HARMONY, ea(1), params = paramMapOf(EventParam.TEXT to "C#7/G"))
    SVP(EventType.HARMONY, EventParam.TONE, Pitch(C, SHARP, 0), ea(1))
    SVP(EventType.HARMONY, EventParam.QUALITY, "7", ea(1))
    SVP(EventType.HARMONY, EventParam.ROOT, Pitch(G, NATURAL, 0), ea(1))
  }

  @Test
  fun testAddHarmonyRoot() {
    SMV()
    SAE(EventType.HARMONY, ea(1), params = paramMapOf(EventParam.TEXT to "C/E"))
    SVP(EventType.HARMONY, EventParam.ROOT, Pitch(E, NATURAL, 0), ea(1))
  }

  @Test
  fun testAddHarmonyRootAccidental() {
    SMV()
    SAE(EventType.HARMONY, ea(1), params = paramMapOf(EventParam.TEXT to "C/F#"))
    SVP(EventType.HARMONY, EventParam.ROOT, Pitch(F, SHARP, 0), ea(1))
  }

  @Test
  fun testAddHarmonyRootFlat() {
    SMV()
    SAE(EventType.HARMONY, ea(1), params = paramMapOf(EventParam.TEXT to "C/Ab"))
    SVP(EventType.HARMONY, EventParam.ROOT, Pitch(A, FLAT, 0), ea(1))
  }

  @Test
  fun testDeleteHarmony() {
    SMV()
    SAE(EventType.HARMONY, ea(1), params = paramMapOf(EventParam.TEXT to "C"))
    SDE(EventType.HARMONY, ea(1))
    SVNE(EventType.HARMONY, ea(1))
  }

  @Test
  fun testDeleteHarmonyIgnoresVoice() {
    SMV()
    SAE(EventType.HARMONY, ea(1), params = paramMapOf(EventParam.TEXT to "C"))
    SDE(EventType.HARMONY, eav(1))
    SVNE(EventType.HARMONY, ea(1))
  }

  @Test
  fun testAddHarmonyMarkerMoves() {
    SMV()
    SSP(EventType.UISTATE, EventParam.MARKER_POSITION, ea(1), eZero())
    SAE(EventType.HARMONY, ea(1), params = paramMapOf(EventParam.TEXT to "C"))
    SVP(EventType.UISTATE, EventParam.MARKER_POSITION, ea(1, crotchet()), eZero())
  }

  @Test
  fun testSetParamByString() {
    SAE(EventType.HARMONY, ea(1), params = paramMapOf(EventParam.TEXT to "C"))
    SSP(EventType.HARMONY, EventParam.TONE, "G", ea(1))
    SVP(EventType.HARMONY, EventParam.TONE, Pitch(G, octave = 0), ea(1))
  }

  @Test
  fun testAddHarmonyToRepeatBar() {
    SAE(EventType.REPEAT_BAR, ea(1), paramMapOf(EventParam.NUMBER to 1))
    SAE(EventType.HARMONY, ea(1), params = paramMapOf(EventParam.TEXT to "C"))
    SVP(EventType.HARMONY, EventParam.TONE, Pitch(C, NATURAL, 0), ea(1))
  }

  @Test
  fun testAddHarmonyToRepeatBarSecondBar() {
    SAE(EventType.REPEAT_BAR, ea(2), paramMapOf(EventParam.NUMBER to 1))
    SAE(EventType.HARMONY, ea(1), params = paramMapOf(EventParam.TEXT to "F"))
    SAE(EventType.HARMONY, ea(2), params = paramMapOf(EventParam.TEXT to "C"))
    SVP(EventType.HARMONY, EventParam.TONE, Pitch(C, NATURAL, 0), ea(2))
  }

  @Test
  fun testAddRepeatBarAfterHarmonySecondBar() {
    SAE(EventType.HARMONY, ea(1), params = paramMapOf(EventParam.TEXT to "F"))
    SAE(EventType.HARMONY, ea(2), params = paramMapOf(EventParam.TEXT to "C"))
    SAE(EventType.REPEAT_BAR, ea(2), paramMapOf(EventParam.NUMBER to 1))
    SVP(EventType.HARMONY, EventParam.TONE, Pitch(C, NATURAL, 0), ea(2))
  }
}
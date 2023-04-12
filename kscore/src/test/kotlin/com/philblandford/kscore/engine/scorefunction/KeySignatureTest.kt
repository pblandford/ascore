package com.philblandford.kscore.engine.scorefunction

import assertEqual
import com.philblandford.kscore.api.Instrument
import com.philblandford.kscore.engine.types.*

import com.philblandford.kscore.engine.dsl.score
import com.philblandford.kscore.engine.duration.minim
import com.philblandford.kscore.engine.pitch.KeySignature
import org.junit.Test

class KeySignatureTest : ScoreTest() {

  @Test
  fun testAddKeySignature() {
    SAE(KeySignature(1).toEvent(), ez(1))
    SVP(EventType.KEY_SIGNATURE, EventParam.SHARPS, 1, ez(1))
  }

  @Test
  fun testAddKeySignatureMidScore() {
    SAE(KeySignature(1).toEvent(), ez(5))
    SVP(EventType.KEY_SIGNATURE, EventParam.SHARPS, 0, ez(1))
    SVP(EventType.KEY_SIGNATURE, EventParam.SHARPS, 1, ez(5))
  }

  @Test
  fun testGetKeySignatureTransposing() {

    val trumpet = Instrument(
      "Trumpet", "", "", 51, -2,
      listOf(ClefType.TREBLE), "", 0
    )
    sc.setNewScore(score {
      part(instrument = trumpet) { }
    })
    SVP(EventType.KEY_SIGNATURE, EventParam.SHARPS, 2, ez(1).copy(staveId = StaveId(1, 1)))
  }

  @Test
  fun testAddKeySignatureMidBar() {
    SAE(KeySignature(2).toEvent(), ez(2, minim()))
    SVP(EventType.KEY_SIGNATURE, EventParam.SHARPS, 2, ez(2))
  }


  @Test
  fun testGetKeySignatureAtTransposing() {

    val trumpet = Instrument(
      "Trumpet", "", "", 51, -2,
      listOf(ClefType.TREBLE), "", 0
    )
    sc.setNewScore(score {
      part(instrument = trumpet) { }
    })
    SVPA(EventType.KEY_SIGNATURE, EventParam.SHARPS, 2, ez(2).copy(staveId = StaveId(1, 1)))
  }

  @Test
  fun testAddKeySignatureVoiceStaveRemoved() {
    SAE(KeySignature(1).toEvent(), eav(2))
    SVP(EventType.KEY_SIGNATURE, EventParam.SHARPS, 1, ez(2))
  }

  @Test
  fun testAddKeySignatureFiveFlats() {
    SAE(KeySignature(-5).toEvent(), ez(1))
    SVP(EventType.KEY_SIGNATURE, EventParam.SHARPS, -5, ez(1))
    SVPA(EventType.KEY_SIGNATURE, EventParam.SHARPS, -5, ez(1))
  }

  @Test
  fun testAddKeySignatureFiveFlatsQueryStave() {
    SAE(KeySignature(-5).toEvent(), ez(1))
    SVP(EventType.KEY_SIGNATURE, EventParam.SHARPS, -5, ea(1))
  }

  @Test
  fun testAddKeySignatureAccidentalsChanged() {
    SMV(72, eventAddress =  eav(3))
    SAE(KeySignature(2).toEvent(), ez(3))
    SVP(
      EventType.NOTE,
      EventParam.PITCH,
      Pitch(NoteLetter.C, Accidental.NATURAL, 5, true),
      eav(3).copy(id = 1)
    )
  }

  @Test
  fun testDeleteKeySignature() {
    SAE(KeySignature(1).toEvent(), ez(3))
    SDE(EventType.KEY_SIGNATURE, ez(3))
    SVNE(EventType.KEY_SIGNATURE, ez(3))
  }

  @Test
  fun testDeleteKeySignatureAccidentalsChanged() {
    SMV(70)
    SAE(KeySignature(1).toEvent(), ez(3))
    SDE(EventType.KEY_SIGNATURE, ez(3))
    Pitch(NoteLetter.C, Accidental.NATURAL, 5, false)
  }

  @Test
  fun testDeleteKeySignatureStartScore() {
    SAE(KeySignature(1).toEvent(), ez(1))
    SDE(EventType.KEY_SIGNATURE, ez(1))
    SVP(EventType.KEY_SIGNATURE, EventParam.SHARPS, 0, ez(1))
  }

  @Test
  fun testAddKeySignatureAfterSameForbidden() {
    SAE(KeySignature(3).toEvent(), eav(1))
    SAE(KeySignature(3).toEvent(), eav(2))
    SVNE(EventType.KEY_SIGNATURE, ez(2))
  }

  @Test
  fun testAddKeySignatureLastButOneSameOK() {
    SAE(KeySignature(3).toEvent(), eav(1))
    SAE(KeySignature(4).toEvent(), eav(2))
    SAE(KeySignature(3).toEvent(), eav(3))
    SVE(EventType.KEY_SIGNATURE, ez(2))
    SVE(EventType.KEY_SIGNATURE, ez(3))
  }


  @Test
  fun testAddKeySignatureRemovesLaterSame() {
    SAE(KeySignature(3).toEvent(), eav(2))
    SAE(KeySignature(3).toEvent(), eav(1))
    SVNE(EventType.KEY_SIGNATURE, ez(2))
  }

  @Test
  fun testAddKeySignatureAccidentalNotChanged() {
    SMV(62)
    SAE(KeySignature(2).toEvent(), ez(1))
    assertEqual(Pitch(NoteLetter.D, Accidental.NATURAL, 4, false),
    EG().getParam(EventType.NOTE, EventParam.PITCH, eav(1).copy(id = 1)))
  }

}
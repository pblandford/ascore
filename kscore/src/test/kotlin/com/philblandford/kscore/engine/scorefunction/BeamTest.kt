package com.philblandford.kscore.engine.scorefunction

import assertEqual
import com.philblandford.kscore.engine.core.representation.LINE_THICKNESS

import com.philblandford.kscore.engine.core.score.*
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.types.*
import grace
import org.junit.Test

class BeamTest : ScoreTest() {

  @Test
  fun testNotesAreBeamed() {
    SMV(duration = quaver())
    SMV(duration = quaver(), eventAddress = eav(1, quaver()))
    SVP(EventType.DURATION, EventParam.IS_BEAMED, true, eav(1))
    SVP(EventType.DURATION, EventParam.IS_BEAMED, true, eav(1, quaver()))
  }

  @Test
  fun testNotesNotBeamedAfterDeleting() {
    SMV(duration = quaver())
    SMV(duration = quaver(), eventAddress = eav(1, quaver()))
    SDE(EventType.DURATION, eav(1))
    SVP(EventType.DURATION, EventParam.IS_BEAMED, false, eav(1, quaver()))
  }

  @Test
  fun testBeamCreated() {
    SMV(duration = quaver())
    SMV(duration = quaver(), eventAddress = eav(1, quaver()))
    assertEqual("8:8", EG().getBeams(eav(1), eav(1)).getBeamStrings().first())
  }

  @Test
  fun testBeamCreatedDownStem() {
    SMV(duration = quaver())
    SMV(duration = quaver(), eventAddress = eav(1, quaver()))
    assert(!EG().getBeams().toList().first().second.up)
  }


  @Test
  fun testBeamCreateUpStem() {
    SMV(60, duration = quaver())
    SMV(60, duration = quaver(), eventAddress = eav(1, quaver()))
    assert(EG().getBeams().toList().first().second.up)
  }


  @Test
  fun testBeamCreatedDownStemMixedNotes() {
    SMV(69, duration = quaver())
    SMV(72, duration = quaver(), eventAddress = eav(1, quaver()))
    SMV(77, duration = quaver(), eventAddress = eav(1, crotchet()))
    SMV(72, duration = quaver(), eventAddress = eav(1, crotchet(1)))
    assert(!EG().getBeams().toList().first().second.up)
    repeat(4) {
      SVP(
        EventType.DURATION,
        EventParam.IS_UPSTEM_BEAM,
        false,
        eav(1, quaver().multiply(it))
      )
    }
  }

  @Test
  fun testTupletNotesBeamed() {
    SAE(tuplet(dZero(), 3, crotchet()).toEvent())
    repeat(3) {
      SMV(
        duration = quaver(),
        eventAddress = eav(1, Duration(1, 12).multiply(it))
      )
    }
    assertEqual("8:8:8", EG().getBeams(eav(1), eav(1)).getBeamStrings().first())
  }


  @Test
  fun testTwoTupletNotesBeamed() {
    SAE(tuplet(dZero(), 3, crotchet()).toEvent())
    repeat(2) {
      SMV(
        duration = quaver(),
        eventAddress = eav(1, Duration(1, 12).multiply(it))
      )
    }
    assertEqual("8:8", EG().getBeams(eav(1), eav(1)).getBeamStrings().first())
  }

  @Test
  fun testTupletNotesBeamedOctaves() {
    SAE(tuplet(dZero(), 3, crotchet()).toEvent())
    SMV(duration = quaver(), eventAddress = eav(1))
    SMV(60, duration = quaver(), eventAddress = eav(1))
    SMV(77, duration = quaver(), eventAddress = eav(1, Duration(1, 12)))
    SMV(65, duration = quaver(), eventAddress = eav(1, Duration(1, 12)))
    assertEqual("8:8", EG().getBeams(eav(1), eav(1)).getBeamStrings().first())
  }

  @Test
  fun testTupletNotesMarkedOctaves() {
    SAE(tuplet(dZero(), 3, crotchet()).toEvent())
    SMV(duration = quaver(), eventAddress = eav(1))
    SMV(60, duration = quaver(), eventAddress = eav(1))
    SMV(77, duration = quaver(), eventAddress = eav(1, Duration(1, 12)))
    SMV(65, duration = quaver(), eventAddress = eav(1, Duration(1, 12)))
    SVP(EventType.DURATION, EventParam.IS_BEAMED, true, eav(1))
    SVP(EventType.DURATION, EventParam.IS_BEAMED, true, eav(1, Duration(1, 12)))
  }

  @Test
  fun testTupletNotesMarkedUpstemOctaves() {
    SAE(tuplet(dZero(), 3, crotchet()).toEvent())
    SMV(duration = quaver(), eventAddress = eav(1))
    SMV(60, duration = quaver(), eventAddress = eav(1))
    SMV(77, duration = quaver(), eventAddress = eav(1, Duration(1, 12)))
    SMV(65, duration = quaver(), eventAddress = eav(1, Duration(1, 12)))
    SVP(EventType.DURATION, EventParam.IS_UPSTEM_BEAM, true, eav(1))
    SVP(EventType.DURATION, EventParam.IS_UPSTEM_BEAM, true, eav(1, Duration(1, 12)))
  }

  @Test
  fun testTwoTupletNotesBeamedAfterRest() {
    SAE(tuplet(dZero(), 3, crotchet()).toEvent())
    repeat(2) {
      SMV(
        duration = quaver(),
        eventAddress = eav(1, Duration(1, 12).multiply(it + 1))
      )
    }
    assertEqual("8:8", EG().getBeams(eav(1), eav(1, Offset(1, 12))).getBeamStrings().first())
  }

  @Test
  fun testTwoTupletNotesAfterRestMembersMarked() {
    SAE(tuplet(dZero(), 3, crotchet()).toEvent())
    repeat(2) {
      SMV(
        duration = quaver(),
        eventAddress = eav(1, Duration(1, 12).multiply(it + 1))
      )
    }
    SVP(EventType.DURATION, EventParam.IS_BEAMED, true, eav(1, Offset(1, 12)))
    SVP(EventType.DURATION, EventParam.IS_BEAMED, true, eav(1, Offset(1, 6)))
  }

  @Test
  fun testTupletNotesBeamedDownStem() {
    SAE(tuplet(dZero(), 3, crotchet()).toEvent())
    repeat(3) {
      SMV(
        duration = quaver(),
        eventAddress = eav(1, Duration(1, 12).multiply(it))
      )
    }
    assert(!EG().getBeams(eav(1), eav(1)).toList().first().second.up)
  }

  @Test
  fun testTupletNotesBeamedAfterBar1() {
    SAE(tuplet(dZero(), 3, crotchet()).toEvent(), eav(5))
    repeat(3) {
      SMV(
        duration = quaver(),
        eventAddress = eav(5, Duration(1, 12).multiply(it))
      )
    }
    assertEqual("8:8:8", EG().getBeams().getBeamStrings().first())
  }

  @Test
  fun testTupletNotesBeamedLastBeat() {
    SAE(tuplet(dZero(), 3, crotchet()).toEvent(), eav(1, minim(1)))
    repeat(3) {
      SMV(
        duration = quaver(),
        eventAddress = eav(1, minim(1).add((Duration(1, 12).multiply(it))))
      )
    }
    assertEqual("8:8:8", EG().getBeams().getBeamStrings().first())
  }

  @Test
  fun testTupletNotesNotBeamedOneNote() {
    SAE(tuplet(dZero(), 3, crotchet()).toEvent())
    SMV(duration = quaver(), eventAddress = eav(1, Duration(1, 6)))
    assertEqual(0, EG().getBeams().size)
  }

  @Test
  fun testQuintupletNotesBeamed() {

    SAE(tuplet(dZero(), 5, minim()).toEvent())
    repeat(5) {
      SMV(
        duration = quaver(),
        eventAddress = eav(1, Duration(1, 10).multiply(it))
      )
    }
    assertEqual("8:8:8:8:8", EG().getBeams(eav(1), eav(1)).getBeamStrings().first())
  }

  @Test
  fun testQuintupletNotesMarkedBeamed() {
    SAE(tuplet(dZero(), 5, minim()).toEvent())
    repeat(5) {
      SMV(
        duration = semiquaver(),
        eventAddress = eav(1, Duration(1, 20).multiply(it))
      )
    }
    repeat(5) {
      SVP(EventType.DURATION, EventParam.IS_BEAMED, true, eav(1, Duration(1, 20).multiply(it)))
    }
  }

  @Test
  fun testDuodecupletNotesBeamed() {

    SAE(tuplet(dZero(), 12, semibreve()).toEvent())
    repeat(12) {
      SMV(
        duration = quaver(),
        eventAddress = eav(1, semibreve().div(12).multiply(it))
      )
    }
    assertEqual(
      "8:8:8:8:8:8:8:8:8:8:8:8",
      EG().getBeams(eav(1), eav(1)).getBeamStrings().first()
    )
  }

  @Test
  fun testDuodecupletNotesBeamed3_8() {

    SAE(TimeSignature(3, 8).toEvent(), ez(1))
    SAE(tuplet(dZero(), 12, crotchet(1)).toEvent())
    repeat(12) {
      SMV(
        duration = demisemiquaver(),
        eventAddress = eav(1, crotchet(1).div(12).multiply(it))
      )
    }
    assertEqual(1, EG().getBeams(eav(1)).size)
    assertEqual(
      "32:32:32:32:32:32:32:32:32:32:32:32",
      EG().getBeams(eav(1), eav(1)).getBeamStrings().first()
    )
  }

  @Test
  fun testNotesNotBeamedSeparate() {
    SMV(duration = quaver())
    SMV(duration = quaver(), eventAddress = eav(1, crotchet()))
    SVP(EventType.DURATION, EventParam.IS_BEAMED, false, eav(1))
    SVP(EventType.DURATION, EventParam.IS_BEAMED, false, eav(1, crotchet()))
  }

  @Test
  fun testTupletNotesMarkedBeamed() {
    SAE(tuplet(dZero(), 3, crotchet()).toEvent())
    repeat(3) {
      SMV(
        duration = quaver(),
        eventAddress = eav(1, Duration(1, 12).multiply(it))
      )
    }
    repeat(3) {
      SVP(EventType.DURATION, EventParam.IS_BEAMED, true, eav(1, Duration(1, 12).multiply(it)))
    }
  }

  @Test
  fun testNotesAreBeamedVoice2() {
    SMV(duration = quaver(), eventAddress = eav(1, dZero(), 2))
    SMV(duration = quaver(), eventAddress = eav(1, quaver(), 2))
    SVP(EventType.DURATION, EventParam.IS_BEAMED, true, eav(1, dZero(), 2))
    SVP(EventType.DURATION, EventParam.IS_BEAMED, true, eav(1, quaver(), 2))
  }

  @Test
  fun testBeamCreatedVoice2() {
    SMV(duration = quaver(), eventAddress = eav(1, dZero(), 2))
    SMV(duration = quaver(), eventAddress = eav(1, quaver(), 2))
    assertEqual(
      "8:8",
      EG().getBeams(eav(1, dZero(), 2), eav(1, dZero(), 2)).getBeamStrings().first()
    )
  }

  @Test
  fun testBeamCreatedDottedNote6_8() {
    SAE(TimeSignature(6, 8).toEvent())
    SMV(duration = quaver(1))
    SMV(duration = semiquaver(), eventAddress = eav(1, quaver(1)))
    SMV(duration = quaver(), eventAddress = eav(1, crotchet()))
    assertEqual("3/16:16:8", EG().getBeams(eav(1), eav(1)).getBeamStrings().first())
  }

  @Test
  fun testGraceNotesBeamed() {
    grace()
    grace()
    SVP(EventType.DURATION, EventParam.IS_BEAMED, true, eagv(1))
    SVP(EventType.DURATION, EventParam.IS_BEAMED, true, eagv(1, graceOffset = semiquaver()))
  }

  @Test
  fun testBeamCreatedGrace() {
    grace()
    grace()
    assertEqual("16:16", EG().getBeams(eav(1)).getBeamStrings().first())
  }

  @Test
  fun testBeamCreatedGraceFourNotes() {
    repeat(4) {
      grace()
    }
    assertEqual("16:16:16:16", EG().getBeams(eav(1)).getBeamStrings().first())
  }

  @Test
  fun testBeamRemainsAfterDeletingGrace() {
    repeat(4) {
      grace()
    }
    SDE(EventType.DURATION, eagv())
    assertEqual("16:16:16", EG().getBeams(eav(1)).getBeamStrings().first())
  }

  @Test
  fun testBeamDoesntAffectNotesInOtherParts() {
    SCD(instruments = listOf("Violin", "Violin"))
    repeat(4) {
      SMV(midiVal = 60, duration = quaver(), eventAddress = eav(1, quaver() * it))
    }
    SMV(midiVal = 72, duration = crotchet(), eventAddress = easv(1, 2, 1))
    val beams = EG().getBeams()
    assertEqual(1, beams.size)
    SVP(EventType.DURATION, EventParam.IS_UPSTEM, false, easv(1, 2, 1))
    SVNP(EventType.DURATION, EventParam.IS_UPSTEM_BEAM, easv(1, 2, 1))
  }

  @Test
  fun testBeamsUpdatedAfterClefChange() {
    SMV(60, quaver())
    SMV(60, quaver(), eventAddress = eav(1, quaver()))
    SAE(EventType.CLEF, ea(1), paramMapOf(EventParam.TYPE to ClefType.BASS))
    val (_, beam) = EG().getBeams().toList().first()
    assertEqual(false, beam.up)
  }

  @Test
  fun testBeamsUpdatedAfterRangeDelete() {
    repeat(8) {
      SMV(midiVal = 60, duration = quaver(), eventAddress = eav(1, quaver() * it))
    }
    SDE(EventType.DURATION, eav(1, crotchet()), eav(1, minim() + quaver()))
    val beams = EG().getBeams().toList().sortedBy { it.first }
    assertEqual(2, beams.size)
    assertEqual(2, beams[0].second.members.size)
    assertEqual(2, beams[1].second.members.size)
  }

  @Test
  fun testGraceBeamsUpdatedAfterNoteShift() {
    repeat(3) {
      grace()
    }
    SMV()
    assertEqual(1, EG().getBeams().size)
    SAE(
      EventType.NOTE_SHIFT, eav(1), paramMapOf(
        EventParam.AMOUNT to 1,
        EventParam.ACCIDENTAL to Accidental.SHARP
      )
    )
    assertEqual(1, EG().getBeams().size)
  }


  @Test
  fun testChangeTransposeOptionDirectionCorrect() {
    SCD(instruments = listOf("Trumpet"))
    SMV(69, duration = quaver())
    SMV(72, duration = quaver(), eventAddress = eav(1, quaver()))

    SSO(EventParam.OPTION_SHOW_TRANSPOSE_CONCERT, true)
    SVP(EventType.DURATION, EventParam.IS_UPSTEM_BEAM, true, eav(1).copy(id = 1))
    SVP(EventType.DURATION, EventParam.IS_UPSTEM_BEAM, true, eav(1, quaver()).copy(id = 1))
  }
}
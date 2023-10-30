package com.philblandford.kscore.engine.scorefunction

import assertEqual
import com.philblandford.kscore.engine.beam.Beam
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.duration.minim
import com.philblandford.kscore.engine.duration.plus
import com.philblandford.kscore.engine.duration.quaver
import com.philblandford.kscore.engine.duration.semibreve
import com.philblandford.kscore.engine.duration.semiquaver
import com.philblandford.kscore.engine.duration.times
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.types.BeamType
import com.philblandford.kscore.engine.types.DurationType
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.eagv
import com.philblandford.kscore.engine.types.eav
import com.philblandford.kscore.engine.types.paramMapOf
import grace
import org.junit.Test
import kotlin.math.min

class UserBeamTest : ScoreTest() {

  @Test
  fun testAddUserBeam() {
    repeat(5) { n ->
      SMV(duration = quaver(), eventAddress = eav(1, quaver().multiply(n)))
    }
    SAE(EventType.BEAM, eav(1), paramMapOf(EventParam.END to eav(1, minim())))
    SVP(EventType.BEAM, EventParam.DURATION, minim(), eav(1))
  }

  @Test
  fun testDeleteUserBeam() {
    repeat(5) { n ->
      SMV(duration = quaver(), eventAddress = eav(1, quaver().multiply(n)))
    }
    SAE(EventType.BEAM, eav(1), paramMapOf(EventParam.END to eav(1, minim())))
    SDE(EventType.BEAM, eav(1))
    SVNE(EventType.BEAM, eav(1))
  }

  @Test
  fun testAddUserBeamGetBeams() {
    repeat(5) { n ->
      SMV(duration = quaver(), eventAddress = eav(1, quaver().multiply(n)))
    }
    SAE(EventType.BEAM, eav(1), paramMapOf(EventParam.END to eav(1, minim())))
    val beams = EG().getBeams().toList().filterNot { it.second.endMarker }
    assertEqual(1, beams.size)
    val beam = beams.first().second
    assertEqual(minim() + quaver(), beam.duration)
    assertEqual(5, beam.members.count())
  }

  @Test
  fun testAddUserBeamChordsMarked() {
    repeat(5) { n ->
      SMV(duration = quaver(), eventAddress = eav(1, quaver().multiply(n)))
    }
    SAE(EventType.BEAM, eav(1), paramMapOf(EventParam.END to eav(1, minim())))
    SVP(EventType.DURATION, EventParam.IS_BEAMED, true, eav(1))
  }

  @Test
  fun testAddUserBeamOthersRemovedIfCompletelyInside() {
    repeat(5) { n ->
      SMV(duration = quaver(), eventAddress = eav(1, quaver().multiply(n)))
    }
    SAE(EventType.BEAM, eav(1), paramMapOf(EventParam.END to eav(1, minim())))
    val beams = EG().getBeams().toList().filterNot { it.second.endMarker }
    assertEqual(1, beams.size)
    verifyBeam(beams[0].second, 5, minim() + quaver())
  }

  @Test
  fun testAddUserBeamOthersShortenedIfStartingBefore() {
    repeat(5) { n ->
      SMV(duration = quaver(), eventAddress = eav(1, quaver().multiply(n)))
    }
    SAE(EventType.BEAM, eav(1, crotchet()), paramMapOf(EventParam.END to eav(1, minim())))
    val beams = EG().getBeams().toList().filterNot { it.second.endMarker }
    assertEqual(2, beams.size)
    verifyBeam(beams[0].second, 2, crotchet())
    verifyBeam(beams[1].second, 3, crotchet(1))
  }

  @Test
  fun testAddUserBeamOthersMovedAndShortenedIfStartingAfter() {
    repeat(5) { n ->
      SMV(duration = quaver(), eventAddress = eav(1, quaver().multiply(n)))
    }
    SAE(EventType.BEAM, eav(1), paramMapOf(EventParam.END to eav(1, quaver(1))))
    val beams = EG().getBeams().toList().filterNot { it.second.endMarker }.sortedBy { it.first }
    assertEqual(2, beams.size)
    verifyBeam(beams[0].second, 2, crotchet())
    verifyBeam(beams[1].second, 2, crotchet())
    assertEqual(eav(1, crotchet()), beams[1].first)
  }

  @Test
  fun testAddUserBeamOthersRemovedIfOnlyOneNoteLeft() {
    repeat(6) { n ->
      SMV(duration = quaver(), eventAddress = eav(1, quaver().multiply(n)))
    }
    SAE(EventType.BEAM, eav(1), paramMapOf(EventParam.END to eav(1, minim())))
    val beams = EG().getBeams().toList().filterNot { it.second.endMarker }.sortedBy { it.first }
    assertEqual(1, beams.size)
    verifyBeam(beams[0].second, 5, Duration(5, 8))
  }

  @Test
  fun testAddUserBeamOthersPreviousRemovedIfOnlyOneNoteLeft() {
    repeat(4) { n ->
      SMV(duration = quaver(), eventAddress = eav(1, quaver().multiply(n)))
    }
    SAE(EventType.BEAM, eav(1, quaver()), paramMapOf(EventParam.END to eav(1, crotchet(1))))
    val beams = EG().getBeams().toList().filterNot { it.second.endMarker }.sortedBy { it.first }
    assertEqual(1, beams.size)
    verifyBeam(beams[0].second, 3, Duration(3, 8))
  }

  @Test
  fun testAddUserBeamNonOverlappingBeamsNotAffected() {
    repeat(8) { n ->
      SMV(duration = quaver(), eventAddress = eav(1, quaver().multiply(n)))
    }
    SAE(EventType.BEAM, eav(1), paramMapOf(EventParam.END to eav(1, crotchet(1))))
    val beams = EG().getBeams().toList().filterNot { it.second.endMarker }.sortedBy { it.first }
    assertEqual(2, beams.size)
    verifyBeam(beams[0].second, 4, minim())
    verifyBeam(beams[1].second, 4, minim())

  }

  @Test
  fun testAddUserBeamOverlapTwoBeams() {
    repeat(8) { n ->
      SMV(duration = quaver(), eventAddress = eav(1, quaver().multiply(n)))
    }
    SAE(EventType.BEAM, eav(1), paramMapOf(EventParam.END to eav(1, minim() + quaver())))
    val beams = EG().getBeams().toList().filterNot { it.second.endMarker }.sortedBy { it.first }
    assertEqual(2, beams.size)
    verifyBeam(beams[0].second, 6, minim(1))
    verifyBeam(beams[1].second, 2, crotchet())

  }

  @Test
  fun testAddUserBeamSecondHalfOfBar() {
    repeat(8) { n ->
      SMV(duration = quaver(), eventAddress = eav(1, quaver().multiply(n)))
    }
    SAE(EventType.BEAM, eav(1, minim()), paramMapOf(EventParam.END to eav(1, minim() + quaver())))
    val beams = EG().getBeams().toList().filterNot { it.second.endMarker }.sortedBy { it.first }
    assertEqual(3, beams.size)
    verifyBeam(beams[0].second, 4, minim())
    verifyBeam(beams[1].second, 2, crotchet())
    verifyBeam(beams[2].second, 2, crotchet())

  }

  @Test
  fun testAddUserBeamGetBeams6and2() {
    repeat(8) { n ->
      SMV(duration = quaver(), eventAddress = eav(1, quaver().multiply(n)))
    }
    SAE(EventType.BEAM, eav(1), paramMapOf(EventParam.END to eav(1, minim() + quaver())))
    val beams = EG().getBeams().toList().filterNot { it.second.endMarker }.sortedBy { it.first }
    assertEqual(2, beams.size)
    assertEqual(eav(1), beams[0].first)
    assertEqual(eav(1, minim(1)), beams[1].first)
    verifyBeam(beams[0].second, 6, minim(1))
    verifyBeam(beams[1].second, 2, crotchet())
  }

  @Test
  fun testAddUserAcrossBars() {
    repeat(2) { bar ->
      repeat(8) { n ->
        SMV(duration = quaver(), eventAddress = eav(bar + 1, quaver() * n))
      }
    }
    SAE(EventType.BEAM, eav(1, minim()), paramMapOf(EventParam.END to eav(2, crotchet(1))))
    val beams = EG().getBeams().toList().filterNot { it.second.endMarker }.sortedBy { it.first }
    assertEqual(3, beams.size)
    verifyBeam(beams[0].second, 4, minim())
    verifyBeam(beams[1].second, 8, semibreve())
    verifyBeam(beams[2].second, 4, minim())
  }

  @Test
  fun testAddUserAcrossBarsChordsMarked() {
    repeat(2) { bar ->
      repeat(8) { n ->
        SMV(duration = quaver(), eventAddress = eav(bar + 1, quaver() * n))
      }
    }
    SAE(EventType.BEAM, eav(1, minim()), paramMapOf(EventParam.END to eav(2, crotchet(1))))
    repeat(4) { n ->
      SVP(EventType.DURATION, EventParam.IS_BEAMED, true, eav(1, minim() + quaver() * n))
    }
    repeat(4) { n ->
      SVP(EventType.DURATION, EventParam.IS_BEAMED, true, eav(2, quaver() * n))
    }
  }

  @Test
  fun testAddUserBeamLastChordMarked() {
    repeat(5) { n ->
      SMV(duration = quaver(), eventAddress = eav(1, quaver().multiply(n)))
    }
    SAE(EventType.BEAM, eav(1), paramMapOf(EventParam.END to eav(1, minim())))
    SVP(EventType.DURATION, EventParam.IS_BEAMED, true, eav(1, minim()))
  }

  @Test
  fun testAddUserBeamBreak() {
    repeat(5) { n ->
      SMV(duration = quaver(), eventAddress = eav(1, quaver().multiply(n)))
    }
    SAE(
      EventType.BEAM,
      eav(1),
      paramMapOf(EventParam.END to eav(1, minim()), EventParam.TYPE to BeamType.BREAK)
    )
    SVP(EventType.BEAM, EventParam.TYPE, BeamType.BREAK, eav(1))
  }

  @Test
  fun testAddUserBeamBreakNoBeams() {
    repeat(5) { n ->
      SMV(duration = quaver(), eventAddress = eav(1, quaver().multiply(n)))
    }
    SAE(
      EventType.BEAM,
      eav(1),
      paramMapOf(EventParam.END to eav(1, minim()), EventParam.TYPE to BeamType.BREAK)
    )
    val beams = EG().getBeams()
    assert(beams.isEmpty())
  }

  @Test
  fun testAddUserBeamLeavesGraceBeams() {
    repeat(4) {
      grace()
    }
    repeat(5) { n ->
      SMV(duration = quaver(), eventAddress = eav(1, quaver().multiply(n)))
    }
    SAE(EventType.BEAM, eav(1), paramMapOf(EventParam.END to eav(1, minim())))
    val beams = EG().getBeams()
    assertEqual(2, beams.size)
  }

  @Test
  fun testAddUserBeamLeavesGraceBeamMembersMarked() {
    repeat(4) {
      grace()
    }
    repeat(5) { n ->
      SMV(duration = quaver(), eventAddress = eav(1, quaver().multiply(n)))
    }
    SAE(EventType.BEAM, eav(1), paramMapOf(EventParam.END to eav(1, minim())))
    repeat(4) {
      SVP(EventType.DURATION, EventParam.IS_BEAMED, true, eagv(graceOffset = semiquaver() * it))
    }
  }

  @Test
  fun testAddUserBeamDeleteNote() {
    repeat(5) { n ->
      SMV(duration = quaver(), eventAddress = eav(1, quaver().multiply(n)))
    }
    SAE(EventType.BEAM, eav(1), paramMapOf(EventParam.END to eav(1, minim())))
    SDE(EventType.DURATION, eav(1))
    val beams = EG().getBeams().toList()
    assertEqual(eav(1, quaver()), beams.first().first)
    verifyBeam(beams.first().second, 4, minim())
  }

  @Test
  fun testDeleteNoteFromUserBeamAdjustsRange() {
    repeat(8) { n ->
      SMV(duration = quaver(), eventAddress = eav(1, quaver().multiply(n)))
    }
    SAE(EventType.BEAM, eav(1), paramMapOf(EventParam.END to eav(1, minim() + crotchet(1))))
    SDE(EventType.DURATION, eav(1, minim() + crotchet(1)))
    SVP(EventType.BEAM, EventParam.DURATION, minim(1), eav(1))
  }

  @Test
  fun testDeleteNoteFromUserBeamStartAdjustsRangeAndAddress() {
    repeat(8) { n ->
      SMV(duration = quaver(), eventAddress = eav(1, quaver().multiply(n)))
    }
    SAE(EventType.BEAM, eav(1), paramMapOf(EventParam.END to eav(1, minim() + crotchet(1))))
    SDE(EventType.DURATION, eav(1))
    SVP(EventType.BEAM, EventParam.DURATION, minim(1), eav(1, quaver()))
  }

  @Test
  fun testDeleteNoteFromUserBeamEndAdjustsBeamDirectory() {
    repeat(8) { n ->
      SMV(duration = quaver(), eventAddress = eav(1, quaver().multiply(n)))
    }
    SAE(EventType.BEAM, eav(1), paramMapOf(EventParam.END to eav(1, minim() + crotchet(1))))
    SDE(EventType.DURATION, eav(1, minim() + crotchet(1)))
    val beams = EG().getBeams().toList()
    verifyBeam(beams.first().second, 7, minim(2))
  }

  @Test
  fun testDeleteNoteFromUserBeamEndLineEndRemoved() {
    repeat(8) { n ->
      SMV(duration = quaver(), eventAddress = eav(1, quaver().multiply(n)))
    }
    SAE(EventType.BEAM, eav(1), paramMapOf(EventParam.END to eav(1, minim() + crotchet(1))))
    SDE(EventType.DURATION, eav(1, minim() + crotchet(1)))
    SVNE(EventType.BEAM, eav(1, minim() + crotchet(1)))
  }

  @Test
  fun testDeleteNoteFromUserBeamStartAdjustsBeamDirectory() {
    repeat(8) { n ->
      SMV(duration = quaver(), eventAddress = eav(1, quaver().multiply(n)))
    }
    SAE(EventType.BEAM, eav(1), paramMapOf(EventParam.END to eav(1, minim() + crotchet(1))))
    SDE(EventType.DURATION, eav(1, minim() + crotchet(1)))
    val beams = EG().getBeams().toList()
    verifyBeam(beams.first().second, 7, minim(2))
  }

  @Test
  fun testDeleteNoteFromUserBeamEndRemainingNotesMarked() {
    repeat(8) { n ->
      SMV(duration = quaver(), eventAddress = eav(1, quaver().multiply(n)))
    }
    SAE(EventType.BEAM, eav(1), paramMapOf(EventParam.END to eav(1, minim() + crotchet(1))))
    SDE(EventType.DURATION, eav(1, minim() + crotchet(1)))
    repeat(7) {
      SVP(EventType.DURATION, EventParam.IS_BEAMED, true, eav(1, quaver() * it))
    }
  }

  @Test
  fun testAddUserBeamBreakStemReverts() {
    SMV(67, duration = quaver())
    SMV(76, duration = quaver(), eventAddress = eav(1, quaver()))
    SAE(
      EventType.BEAM,
      eav(1),
      paramMapOf(EventParam.END to eav(1, quaver()), EventParam.TYPE to BeamType.BREAK)
    )
    SVP(EventType.DURATION, EventParam.IS_UPSTEM, false, eav(1, quaver()))
    SVNP(EventType.DURATION, EventParam.IS_UPSTEM_BEAM, eav(1, quaver()))
  }

  @Test
  fun testDeleteEventInUserBreakGroup() {
    SMV(67, duration = quaver())
    SMV(76, duration = quaver(), eventAddress = eav(1, quaver()))
    SAE(
      EventType.BEAM,
      eav(1),
      paramMapOf(EventParam.END to eav(1, quaver()), EventParam.TYPE to BeamType.BREAK)
    )
    SDE(EventType.DURATION, eav(1))
    SVP(EventType.DURATION, EventParam.TYPE, DurationType.REST, eav(1))
  }

  @Test
  fun testDeleteSecondEventInUserBreakGroup() {
    SMV(67, duration = quaver())
    SMV(76, duration = quaver(), eventAddress = eav(1, quaver()))
    SAE(
      EventType.BEAM,
      eav(1),
      paramMapOf(EventParam.END to eav(1, quaver()), EventParam.TYPE to BeamType.BREAK)
    )
    SDE(EventType.DURATION, eav(1))
    SDE(EventType.DURATION, eav(1, quaver()))
    SVNE(EventType.DURATION, eav(1))
  }

  @Test
  fun testAddUserBeamVoice2() {
    repeat(5) { n ->
      SMV(duration = quaver(), eventAddress = eav(1, quaver().multiply(n), voice = 2))
    }
    SAE(EventType.BEAM, eav(1, voice = 2), paramMapOf(EventParam.END to eav(1, minim(), voice = 2)))
    SVP(EventType.BEAM, EventParam.DURATION, minim(), eav(1, voice = 2))
  }

  @Test
  fun testAddUserBeamVoice2BeamCreated() {
    repeat(5) { n ->
      SMV(duration = quaver(), eventAddress = eav(1, quaver().multiply(n), voice = 2))
    }
    SAE(EventType.BEAM, eav(1, voice = 2), paramMapOf(EventParam.END to eav(1, minim(), voice = 2)))
    val (key, beam) = EG().getBeams().toList().first()
    assertEqual(eav(1, voice = 2), key)
    verifyBeam(beam, 5, minim() + quaver())
  }

  @Test
  fun testAddUserBeamEndIsRest() {
    repeat(5) { n ->
      SMV(duration = quaver(), eventAddress = eav(1, quaver().multiply(n)))
    }
    SAE(EventType.BEAM, eav(1), paramMapOf(EventParam.END to eav(1, minim(1))))
    val (key, beam) = EG().getBeams().toList().first()
    verifyBeam(beam, 5, minim() + quaver())
  }

  @Test
  fun testAddUserBeamsOverTuplets() {
    SAE(
      EventType.TUPLET,
      eav(1),
      paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8)
    )
    SAE(
      EventType.TUPLET,
      eav(1, crotchet()),
      paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8)
    )
    repeat(6) { num ->
      SMV(duration = quaver(), eventAddress = eav(1, Duration(1,12) * num))
    }
    SAE(EventType.BEAM, eav(1), paramMapOf(EventParam.END to eav(1, minim())))
    val beams = EG().getBeams().toList()
    assertEqual(1, beams.size)
    verifyBeam(beams.first().second, 6, minim())
  }

  @Test
  fun testAddUserBeam12_8() {
    SCD(timeSignature = TimeSignature(12, 8))
    repeat(12) { num ->
      SMV(duration = quaver(), eventAddress = eav(1, quaver() * num))
    }
    SAE(EventType.BEAM, eav(1), paramMapOf(EventParam.END to eav(1, quaver()* 11)))
    val beams = EG().getBeams().toList()
    assertEqual(1, beams.size)
  }

  @Test
  fun testAddUserBeam12_8_Two_Beams() {
    SCD(timeSignature = TimeSignature(12, 8))
    repeat(12) { num ->
      SMV(duration = quaver(), eventAddress = eav(1, quaver() * num))
    }
    SAE(EventType.BEAM, eav(1), paramMapOf(EventParam.END to eav(1, quaver()* 5)))
    SAE(EventType.BEAM, eav(1, quaver() * 6),
      paramMapOf(EventParam.END to eav(1, quaver()* 11)))
    val beams = EG().getBeams().toList()
    assertEqual(2, beams.size)
  }

  @Test
  fun testAddUserBeamOverGraceGroupIsNoop() {
    repeat(3) {
      grace()
    }
    SAE(EventType.BEAM, eagv(1), paramMapOf(EventParam.TYPE to BeamType.JOIN), eagv(1, graceOffset = quaver()))
    val beams = EG().getBeams().toList()
    assertEqual(1, beams.size)
    verifyBeam(beams.first().second, 3, quaver(1))
  }

  @Test
  fun testAddUserBeamGraceInMiddleNotIncluded() {
    repeat(8) {
      SMV(duration = quaver(), eventAddress = eav(1, quaver() * it))
    }
    repeat(3) {
      grace(mainOffset = crotchet())
    }
    SAE(EventType.BEAM, eav(1), paramMapOf(EventParam.TYPE to BeamType.JOIN), eav(1, minim(2)))
    val beams = EG().getBeams().toList().sortedBy { it.first.offset }
    assertEqual(2, beams.size)
    verifyBeam(beams[0].second, 8, semibreve())
    verifyBeam(beams[1].second, 3, quaver(1))
  }

  @Test
  fun testBeamCrotchetsIsNoOp() {
    repeat(4) {
      SMV(eventAddress = eav(1, crotchet() * it))
    }
    SAE(EventType.BEAM, eav(1), paramMapOf(EventParam.TYPE to BeamType.JOIN), eav(1, minim(1)))
    assertEqual(0, EG().getBeams().size)
  }

  private fun verifyBeam(beam: Beam, members: Int, duration: Duration) {
    assertEqual(members, beam.members.count())
    assertEqual(duration, beam.duration)
  }
}
package com.philblandford.kscore.engine.beam

import assertEqual
import com.philblandford.kscore.api.Instrument
import com.philblandford.kscore.engine.core.score.Tuplet
import com.philblandford.kscore.engine.core.score.tuplet
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.duration.Offset
import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.duration.minim
import com.philblandford.kscore.engine.duration.plus
import com.philblandford.kscore.engine.duration.quaver
import com.philblandford.kscore.engine.duration.times
import com.philblandford.kscore.engine.eventadder.ok
import com.philblandford.kscore.engine.eventadder.rightOrThrow
import com.philblandford.kscore.engine.scorefunction.ScoreTest
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.StaveId
import com.philblandford.kscore.engine.types.easv
import com.philblandford.kscore.engine.types.eav
import com.philblandford.kscore.log.ksLoge
import org.apache.commons.math3.fraction.Fraction
import org.junit.Test


internal class BeamDirectoryTest : ScoreTest() {

  @Test
  fun testCreateBeamsOneBar() {
    createQuavers()
    val beamDirectory = BeamDirectory.create(EG())
    val beams = beamDirectory.getBeamsForStave(StaveId(1, 1), 1, 10, EG()).toList()
    assertEqual(
      Beam(
        (0..3).map { BeamMember(quaver() * it, quaver(), quaver()) },
        minim(),
        false
      ), beams.first().second
    )
  }

  @Test
  fun testCreateBeamsTwoBars() {
    createQuavers()
    createQuavers(2)
    val beamDirectory = BeamDirectory.create(EG())
    val beams = beamDirectory.getBeamsForStave(StaveId(1, 1), 1, 10, EG()).toList().sortedBy { it.first.barNum }
    assertEqual("8:8:8:8", beams[0].second.getBeamString())
    assertEqual("8:8:8:8", beams[1].second.getBeamString())
  }

  @Test
  fun testCreateBeamsTwoStaves() {
    SCD(instruments = listOf(Instrument.default().name, Instrument.default().name))
    createQuavers()
    createQuavers(staveId = StaveId(2,1))
    val beamDirectory = BeamDirectory.create(EG())
    var beams = beamDirectory.getBeamsForStave(StaveId(1, 1), 1, 10, EG()).toList().sortedBy { it.first.barNum }
    assertEqual("8:8:8:8", beams[0].second.getBeamString())
    assertEqual(StaveId(1,1), beams.first().first.staveId)
    beams = beamDirectory.getBeamsForStave(StaveId(2, 1), 1, 10, EG()).toList().sortedBy { it.first.barNum }
    assertEqual("8:8:8:8", beams[0].second.getBeamString())
    assertEqual(StaveId(2,1), beams.first().first.staveId)
  }

  @Test
  fun testCreateBeamsChordsMarked() {
    createQuavers()
    val beamDirectory = BeamDirectory.create(EG())
    val score = beamDirectory.markBeamGroupMembers(EG()).rightOrThrow()
    setNewScore(score)
    SVP(EventType.DURATION, EventParam.IS_BEAMED, true, eav(1))
  }

  @Test
  fun testCreateBeamsLimited() {
    createQuavers()
    createQuavers(2)
    val beamDirectory = BeamDirectory.create(EG(), listOf(eav(1)))
    val beams = beamDirectory.getBeamsForStave(StaveId(1, 1), 1, 10, EG()).toList().sortedBy { it.first.barNum }
    assertEqual("8:8:8:8", beams[0].second.getBeamString())
    assert(beams.getOrNull(1) == null)
  }


  @Test
  fun testUpdate() {
    createQuavers()
    createQuavers(2)
    var beamDirectory = BeamDirectory.create(EG(), listOf(eav(1)))
    beamDirectory = beamDirectory.update(EG(), listOf(eav(2)))
    val beams = beamDirectory.getBeamsForStave(StaveId(1, 1), 1, 10, EG()).toList().sortedBy { it.first.barNum }
    assertEqual("8:8:8:8", beams[0].second.getBeamString())
    assertEqual("8:8:8:8", beams[1].second.getBeamString())
  }

  @Test
  fun testCreateBeamsTuplet() {
    createTupletQuavers()
    val beamDirectory = BeamDirectory.create(EG())
    val beams = beamDirectory.getBeamsForStave(StaveId(1, 1), 1, 10, EG()).toList()
    assertEqual(
      Beam(
        (0..2).map { BeamMember(tupletQuaver * it, quaver(), tupletQuaver) },
        Duration(3,8),
        false
      ), beams.first().second
    )
  }

  @Test
  fun testCreateBeamsTupletChordsMarked() {
    createTupletQuavers()
    val beamDirectory = BeamDirectory.create(EG())
    val score = beamDirectory.markBeamGroupMembers(EG()).rightOrThrow()
    setNewScore(score)
    repeat(3) {
      SVP(EventType.DURATION, EventParam.IS_BEAMED, true, eav(1, offset = tupletQuaver * it))
    }
  }

  @Test
  fun testCreateBeamsTwoTuplets() {
    createTupletQuavers()
    createTupletQuavers(offset = crotchet())
    val beamDirectory = BeamDirectory.create(EG())
    val beams = beamDirectory.getBeamsForStave(StaveId(1, 1), 1, 10, EG()).toList()
    assertEqual(
      Beam(
        (0..2).map { BeamMember(tupletQuaver * it, quaver(), tupletQuaver) },
        Duration(3,8),
        false
      ), beams.first().second
    )
    assertEqual(
      Beam(
        (0..2).map { BeamMember(tupletQuaver * it, quaver(), tupletQuaver) },
        Duration(3,8),
        false
      ), beams[1].second
    )
    assertEqual(eav(1, crotchet()), beams[1].first)
  }

  @Test
  fun testCreateBeamsTwoTupletsChordsMarked() {
    createTupletQuavers()
    createTupletQuavers(offset = crotchet())
    val beamDirectory = BeamDirectory.create(EG())
    val score = beamDirectory.markBeamGroupMembers(EG()).rightOrThrow()
    setNewScore(score)
    repeat(3) {
      SVP(EventType.DURATION, EventParam.IS_BEAMED, true, eav(1, offset = tupletQuaver * it))
    }
    repeat(3) {
      SVP(EventType.DURATION, EventParam.IS_BEAMED, true, eav(1, offset = crotchet() + tupletQuaver * it))
    }
  }

  private fun createQuavers(barNum:Int = 1, staveId: StaveId = StaveId(1,1), numQuavers:Int = 4) {
    repeat(numQuavers) {
      SMV(duration = quaver(), eventAddress = easv(barNum, quaver() * it, staveId))
    }
  }

  private fun createTupletQuavers(barNum:Int = 1, offset:Offset = dZero(),  staveId: StaveId = StaveId(1, 1), numQuavers:Int = 3) {
    SAE(tuplet(offset, 3, 8).toEvent(), eav(1, offset))
    repeat(numQuavers) {
      SMV(duration = quaver(), eventAddress = easv(barNum, offset + tupletQuaver * it, staveId))
    }
  }
  private val tupletQuaver = quaver().multiply(Duration(2, 3))

}
package com.philblandford.kscore.engine.beam

import BeamDescriptor
import BeamState
import assertEqual
import beamStateQuery
import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.duration.quaver
import com.philblandford.kscore.engine.duration.semiquaver
import getBeamDescriptors
import getDuration
import org.junit.Test
import toHash

class BeamDescriptorTest {

  @Test
  fun testTwoQuavers() {
    val beam = getBeam("8:8")
    val descriptors = getBeamDescriptors(beam)
    assertEqual(listOf(BeamDescriptor(quaver(), listOf(dZero(), quaver()))), descriptors.toList())
  }

  @Test
  fun testTwoSemiQuavers() {
    val beam = getBeam("16:16")
    val descriptors = getBeamDescriptors(beam)
    assertEqual(listOf(
      BeamDescriptor(quaver(), listOf(dZero(), semiquaver())),
      BeamDescriptor(semiquaver(), listOf(dZero(), semiquaver()))
      ).toList(), descriptors.toList())
  }

  @Test
  fun testQuaverTwoSemiQuavers() {
    val beam = getBeam("8:16:16")
    val descriptors = getBeamDescriptors(beam)
    assertEqual(listOf(
      BeamDescriptor(quaver(), listOf(dZero(), quaver(), quaver(1))),
      BeamDescriptor(semiquaver(), listOf(quaver(), quaver(1)))
    ).toList(), descriptors.toList())
  }

  @Test
  fun testDottedQuaverSemiquaver() {
    val beam = getBeam("3/16:16")
    val descriptors = getBeamDescriptors(beam)
    assertEqual(listOf(
      BeamDescriptor(quaver(), listOf(dZero(), quaver(1))),
      BeamDescriptor(semiquaver(), listOf(quaver(1)))
    ).toList(), descriptors.toList())
  }

  @Test
  fun testSemiquaverQuaverSemiquaver() {
    val beam = getBeam("16:8:16")
    val descriptors = getBeamDescriptors(beam)
    assertEqual(listOf(
      BeamDescriptor(quaver(), listOf(dZero(), semiquaver(), quaver(1))),
      BeamDescriptor(semiquaver(), listOf(dZero())),
      BeamDescriptor(semiquaver(), listOf(quaver(1)))
    ).toList(), descriptors.toList())
  }

  @Test
  fun testDottedQuaverSemiquaverQuaver() {
    val beam = getBeam("3/16:16:8")
    val descriptors = getBeamDescriptors(beam)
    assertEqual(listOf(
      BeamDescriptor(quaver(), listOf(dZero(), quaver(1), crotchet())),
      BeamDescriptor(semiquaver(), listOf(quaver(1)))
    ).toList(), descriptors.toList())
  }

  @Test
  fun testBeamDescriptorQuery() {
    val beams = createBeams("C8:C8".toHash())
    val query = beamStateQuery(beams)
    assertEqual(listOf(BeamState(quaver(), BeamPos.START)).toList(), query.getState(dZero()).toList())
  }

  @Test
  fun testBeamDescriptorQueryEndBeam() {
    val beams = createBeams("C8:C8".toHash())
    val query = beamStateQuery(beams)
    assertEqual(listOf(BeamState(quaver(), BeamPos.END)).toList(), query.getState(quaver()).toList())
  }

  @Test
  fun testBeamDescriptorQueryMidBeam() {
    val beams = createBeams("C8:C8:C8".toHash())
    val query = beamStateQuery(beams)
    assertEqual(listOf(BeamState(quaver(), BeamPos.MID)).toList(), query.getState(quaver()).toList())
  }

  @Test
  fun testBeamDescriptorQuerySemiquavers() {
    val beams = createBeams("C16:C16".toHash())
    val query = beamStateQuery(beams)
    assertEqual(listOf(BeamState(quaver(), BeamPos.START),
      BeamState(semiquaver(), BeamPos.START)).toList(), query.getState(dZero()).toList())
  }

  private fun getBeam(string: String): Beam {
    val fields = string.split(":")
    val durations = fields.map { BeamMember(dZero(),getDuration(it), getDuration(it)) }
    val duration = durations.reduce { x, y -> val d = x.duration.add(y.duration); BeamMember(dZero(), d,d) }

    return Beam(durations, duration.duration)
  }
}
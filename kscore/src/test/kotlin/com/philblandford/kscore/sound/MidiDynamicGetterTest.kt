package com.philblandford.kscore.sound

import assertEqual
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.duration.dZero

import org.junit.Test
import com.philblandford.kscore.engine.scorefunction.ScoreTest

class MidiDynamicGetterTest : ScoreTest() {

  @Test
  fun testMidiDynamicGetter() {
    fillScore(4)
    SAE(EventType.DYNAMIC, ea(1), paramMapOf(EventParam.TYPE to DynamicType.FORTE))
    val mdg = MidiDynamicGetter(EG())
    assertEqual(velocities[DynamicType.FORTE], mdg.getVelocity(ea(1)))
  }

  @Test
  fun testMidiDynamicGetterTwoStaves() {
    SCDG()
    fillScore(4, listOf(StaveId(1,1), StaveId(1,2)))
    SAE(EventType.DYNAMIC, ea(1), paramMapOf(EventParam.TYPE to DynamicType.FORTE))
    SAE(EventType.DYNAMIC, ea(1).copy(staveId = StaveId(1,2)),
      paramMapOf(EventParam.TYPE to DynamicType.PIANISSIMO))
    val mdg = MidiDynamicGetter(EG())
    assertEqual(velocities[DynamicType.FORTE], mdg.getVelocity(ea(1)))
    assertEqual(velocities[DynamicType.PIANISSIMO], mdg.getVelocity(eas(1, dZero(), StaveId(1,2))))
  }

  @Test
  fun testMidiDynamicGetterTwoStavesOneUnspecified() {
    SCDG()
    fillScore(4, listOf(StaveId(1,1), StaveId(1,2)))
    SAE(EventType.DYNAMIC, ea(1).copy(staveId = StaveId(1,2)),
      paramMapOf(EventParam.TYPE to DynamicType.PIANISSIMO))
    val mdg = MidiDynamicGetter(EG())
    assertEqual(velocities[DynamicType.PIANISSIMO], mdg.getVelocity(ea(1)))
    assertEqual(velocities[DynamicType.PIANISSIMO], mdg.getVelocity(eas(1, dZero(), StaveId(1,2))))
  }

  @Test
  fun testMidiDynamicGetterTwoStavesMostRecentPrevails() {
    SCDG()
    fillScore(4, listOf(StaveId(1,1), StaveId(1,2)))
    SAE(EventType.DYNAMIC, ea(1).copy(staveId = StaveId(1,2)),
      paramMapOf(EventParam.TYPE to DynamicType.PIANISSIMO))
    SAE(EventType.DYNAMIC, ea(2),
      paramMapOf(EventParam.TYPE to DynamicType.FORTE))
    val mdg = MidiDynamicGetter(EG())
    assertEqual(velocities[DynamicType.FORTE], mdg.getVelocity(ea(2)))
    assertEqual(velocities[DynamicType.FORTE], mdg.getVelocity(eas(2, dZero(), StaveId(1,2))))
  }

  private fun fillScore(bars:Int, staves:Iterable<StaveId> = listOf(StaveId(1,1))) {
    staves.forEach { staveId ->
      repeat(bars) { bar ->
        repeat(4) { offset ->
          SMV(eventAddress = eav(bar + 1, crotchet().multiply(offset)).copy(staveId = staveId))
        }
      }
    }
  }
}
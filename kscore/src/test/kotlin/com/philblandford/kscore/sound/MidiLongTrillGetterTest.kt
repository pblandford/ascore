package com.philblandford.kscore.sound




import com.philblandford.kscore.engine.scorefunction.ScoreTest
import com.philblandford.kscore.engine.types.*
import org.junit.Test

class MidiLongTrillGetterTest : ScoreTest() {

  @Test
  fun testGetLongTrill() {
    SMV()
    SMV(eventAddress =  eav(2))
    SAE(EventType.LONG_TRILL, ea(1), paramMapOf(EventParam.END to ea(2)))
    val ltg = MidiLongTrillGetter(EG())
    assert(ltg.longTrillActive(ea(1)) != null)
  }

  @Test
  fun testGetLongEnd() {
    SMV()
    SMV(eventAddress =  eav(2))
    SAE(EventType.LONG_TRILL, ea(1), paramMapOf(EventParam.END to ea(2)))
    val ltg = MidiLongTrillGetter(EG())
    assert(ltg.longTrillActive(ea(2)) != null)
  }

  @Test
  fun testGetLongTrillNotAfter() {
    SMV()
    SMV(eventAddress =  eav(2))
    SMV(eventAddress =  eav(3))
    SAE(EventType.LONG_TRILL, ea(1), paramMapOf(EventParam.END to ea(2)))
    val ltg = MidiLongTrillGetter(EG())
    assert(ltg.longTrillActive(ea(3)) == null)
  }
}
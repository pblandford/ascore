package com.philblandford.kscore.engine.core.endtoend

import assertEqual
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.eav
import com.philblandford.kscore.engine.types.paramMapOf


import com.philblandford.kscore.engine.duration.demisemiquaver
import com.philblandford.kscore.engine.duration.hemidemisemiquaver
import com.philblandford.kscore.engine.duration.quaver
import com.philblandford.kscore.engine.duration.semiquaver
import com.philblandford.kscore.engine.core.representation.RepTest
import org.junit.Test

class TremoloTest : RepTest() {

  @Test
  fun testTremolo() {
    SMV()
    SAE(EventType.TREMOLO, eav(1), paramMapOf(EventParam.TREMOLO_BEATS to quaver()))
    RVA("Tremolo", eav(1))
  }

  @Test
  fun testTremoloUpStem() {
    SMV(60)
    SAE(EventType.TREMOLO, eav(1), paramMapOf(EventParam.TREMOLO_BEATS to quaver()))
    assert(isAbove("Tremolo", eav(1), "Tadpole", eav(1).copy(id = 1))!!)
  }

  @Test
  fun testTremoloDownStem() {
    SMV(72)
    SAE(EventType.TREMOLO, eav(1), paramMapOf(EventParam.TREMOLO_BEATS to quaver()))
    assert(isAbove("Tadpole", eav(1).copy(id = 1), "Tremolo", eav(1))!!)
  }

  @Test
  fun testTremoloNumStrokes() {
    SMV()
    SAE(EventType.TREMOLO, eav(1), paramMapOf(EventParam.TREMOLO_BEATS to quaver()))
    assertEqual(1, getAreas("TremoloStroke").count())
  }

  @Test
  fun testTremoloNumStrokes2() {
    SMV()
    SAE(EventType.TREMOLO, eav(1), paramMapOf(EventParam.TREMOLO_BEATS to semiquaver()))
    assertEqual(2, getAreas("TremoloStroke").count())
  }

  @Test
  fun testTremoloNumStrokes3() {
    SMV()
    SAE(EventType.TREMOLO, eav(1), paramMapOf(EventParam.TREMOLO_BEATS to demisemiquaver()))
    assertEqual(3, getAreas("TremoloStroke").count())
  }

  @Test
  fun testTremoloNumStrokes4() {
    SMV()
    SAE(EventType.TREMOLO, eav(1), paramMapOf(EventParam.TREMOLO_BEATS to hemidemisemiquaver()))
    assertEqual(4, getAreas("TremoloStroke").count())
  }
}
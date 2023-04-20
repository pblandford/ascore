package com.philblandford.kscore.engine.core.endtoend

import assertEqual
import com.philblandford.kscore.engine.core.representation.RepTest
import com.philblandford.kscore.engine.types.*


import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.duration.minim

import org.junit.Test

class LongTrillTest : RepTest() {

  @Test
  fun testAddLongTrill() {
    SAE(EventType.LONG_TRILL, params = paramMapOf(EventParam.END to ea(2), EventParam.IS_UP to true))
    RVA("LongTrill", ea(1))
  }

  @Test
  fun testAddLongTrillOnlyOne() {
    SAE(EventType.LONG_TRILL, params = paramMapOf(EventParam.END to ea(2), EventParam.IS_UP to true))
    val areas = getAreas("LongTrill")
    assertEqual(1, areas.size)
  }

  @Test
  fun testAddLongTrillOverhang() {
    SAE(EventType.LONG_TRILL, params = paramMapOf(EventParam.END to ea(EG().numBars), EventParam.IS_UP to true))
    val areas = getAreas("LongTrill")
    assert(areas.size > 1)
  }

  @Test
  fun testAddLongTrillOverhangHasLines() {
    val staveStart = getStaveBar(1)
    SAE(
      EventType.LONG_TRILL, ea(staveStart - 2),
      paramMapOf(EventParam.END to ea(staveStart + 1), EventParam.IS_UP to true)
    )
    val areas = getAreas("LongTrillLine")
    assert(areas.size == 2)
    areas.forEach{
      assert(it.value.width > 0)
      assert(it.value.height > 0)
    }
  }

  @Test
  fun testAddLongTrillOverhangHasLinesOverNotes() {
    repeat(10) {bar ->
      repeat(4) { offset ->
        SMV(eventAddress = eav(bar+1, crotchet().multiply(offset)))
      }
    }

    val staveStart = getStaveBar(1)
    SAE(
      EventType.LONG_TRILL, ea(staveStart - 2),
      paramMapOf(EventParam.END to ea(staveStart + 1), EventParam.IS_UP to true)
    )
    val areas = getAreas("LongTrillLine")
    assert(areas.size == 2)
    areas.forEach{
      assert(it.value.width > 0)
      assert(it.value.height > 0)
    }
  }

  @Test
  fun testAddLongTrillBarOfCrotchets() {
    repeat(4) { SMV(eventAddress = eav(1, crotchet().multiply(it))) }
    SAE(
      EventType.LONG_TRILL,
      params = paramMapOf(EventParam.END to ea(1, minim(1)), EventParam.IS_UP to true)
    )
    val trill = getArea("LongTrill", ea(1))!!
    val note = getArea("Tadpole", eav(1, minim(1)).copy(id = 1))!!
    assert(trill.coord.x + trill.area.width > note.coord.x)
  }

  @Test
  fun testAddLongTrillAccidental() {
    SAE(EventType.LONG_TRILL, params = paramMapOf(EventParam.END to ea(2), EventParam.IS_UP to true,
      EventParam.ACCIDENTAL_ABOVE to Accidental.FLAT))
    assertEqual(1, getAreas("LongTrillAccidental").size)
  }

}
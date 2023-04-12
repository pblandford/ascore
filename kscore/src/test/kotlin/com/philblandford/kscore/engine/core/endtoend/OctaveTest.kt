package com.philblandford.kscore.engine.core.endtoend

import assertEqual
import com.philblandford.kscore.engine.types.*


import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.duration.minim

import core.representation.*
import org.junit.Test

class OctaveTest : RepTest() {

  @Test
  fun testAddOctave() {
    SAE(EventType.OCTAVE, params = paramMapOf(EventParam.END to ea(2), EventParam.NUMBER to 8))
    RVA("Octave", ea(1))
  }

  @Test
  fun testAddOctaveOnlyOne() {
    SAE(EventType.OCTAVE, params = paramMapOf(EventParam.END to ea(2), EventParam.NUMBER to 1))
    val areas = getAreas("Octave")
    assertEqual(1, areas.size)
  }

  @Test
  fun testAddOctaveBelow() {
    SMV(60)
    SAE(EventType.OCTAVE, params = paramMapOf(EventParam.END to ea(2), EventParam.NUMBER to -1))
    val octave = RCoord("Octave", ea(1))!!
    val note = RCoord("Tadpole", eav(1).copy(id = 1))!!
    assert(octave.y > note.y)
  }

  @Test
  fun testAddOctaveOverhang() {
    SAE(EventType.OCTAVE, params = paramMapOf(EventParam.END to ea(EG().numBars), EventParam.NUMBER to 1))
    val areas = getAreas("Octave")
    assert(areas.size > 1)
  }

  @Test
  fun testAddOctaveOverhangHasLines() {
    val staveStart = getStaveBar(1)
    SAE(
      EventType.OCTAVE, ea(staveStart - 2),
      paramMapOf(EventParam.END to ea(staveStart + 1), EventParam.NUMBER to 1)
    )
    val areas = getAreas("OctaveLine")
    assert(areas.size == 2)
    areas.forEach{
      assert(it.value.width > 0)
      assert(it.value.height > 0)
    }
  }

  @Test
  fun testAddOctaveOverhangHasLinesOverNotes() {
    repeat(10) {bar ->
      repeat(4) { offset ->
        SMV(eventAddress = eav(bar+1, crotchet().multiply(offset)))
      }
    }

    val staveStart = getStaveBar(1)
    SAE(
      EventType.OCTAVE, ea(staveStart - 2),
      paramMapOf(EventParam.END to ea(staveStart + 1), EventParam.NUMBER to 1)
    )
    val areas = getAreas("OctaveLine")
    assert(areas.size == 2)
    areas.forEach{
      assert(it.value.width > 0)
      assert(it.value.height > 0)
    }
  }

  @Test
  fun testAddOctaveBarOfCrotchets() {
    repeat(4) { SMV(eventAddress = eav(1, crotchet().multiply(it))) }
    SAE(
      EventType.OCTAVE,
      params = paramMapOf(EventParam.END to ea(1, minim(1)), EventParam.NUMBER to 1)
    )
    val octave = getArea("Octave", ea(1))!!
    val note = getArea("Tadpole", eav(1, minim(1)).copy(id = 1))!!
    assert(octave.coord.x + octave.area.width > note.coord.x + note.area.width)
  }
}
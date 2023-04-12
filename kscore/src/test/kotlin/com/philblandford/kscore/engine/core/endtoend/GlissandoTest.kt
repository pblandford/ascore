package com.philblandford.kscore.engine.core.endtoend

import com.philblandford.kscore.engine.types.*


import com.philblandford.kscore.engine.duration.crotchet
import core.representation.*
import org.junit.Test

class GlissandoTest : RepTest() {

  @Test
  fun testGlissando() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    SAE(EventType.GLISSANDO, params = paramMapOf(EventParam.TYPE to GlissandoType.LINE))
    RVA("Glissando", ea(1))
  }

  @Test
  fun testGlissandoRightOfStart() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    SAE(EventType.GLISSANDO, params = paramMapOf(EventParam.TYPE to GlissandoType.LINE))
    assert(isLeft("Tadpole", eav(1).copy(id = 1), "Glissando", ea(1)) == true)
  }

  @Test
  fun testGlissandoLeftOfEnd() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    SAE(EventType.GLISSANDO, params = paramMapOf(EventParam.TYPE to GlissandoType.LINE))
    assert(isLeft("Glissando", ea(1), "Tadpole", eav(1, crotchet()).copy(id = 1)) == true)
  }

  @Test
  fun testGlissandoStartsAtNote() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    SAE(EventType.GLISSANDO, params = paramMapOf(EventParam.TYPE to GlissandoType.LINE))
    assert(
      isInsideY(
        getArea("Glissando", ea(1))?.coord?.y!!,
        "Tadpole",
        eav(1, crotchet()).copy(id = 1)
      ) == true
    )
  }

  @Test
  fun testGlissandoIncreasesSliceSize() {
    SMV()
    SMV(60, eventAddress = eav(1, crotchet()))
    val old = getArea("Chord", eav(1, crotchet()))!!.coord.x -
        getArea("Chord", eav(1))!!.coord.x
    SAE(EventType.GLISSANDO, params = paramMapOf(EventParam.TYPE to GlissandoType.LINE))
    val new = getArea("Chord", eav(1, crotchet()))!!.coord.x -
        getArea("Chord", eav(1))!!.coord.x
    assert(new > old)
  }
}
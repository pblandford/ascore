package com.philblandford.kscore.engine.core.endtoend

import com.philblandford.kscore.engine.types.eav


import com.philblandford.kscore.engine.dsl.rest
import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.duration.minim
import core.representation.RepTest
import org.junit.Test

class RestTest : RepTest() {

  @Test
  fun testRestCreated() {
    SAE(rest(), eav(1))
    RVA("Rest", eav(1))
  }

  @Test
  fun testRestDotCreated() {
    SAE(rest(crotchet(1)), eav(1))
    RVA("Dot", eav(1))
  }

  @Test
  fun testVoice2RestBelowNote() {
    SMV(60, duration = minim())
    SMV(55, eventAddress = eav(1, crotchet(), 2))
    val note = getArea("Tadpole", eav(1).copy(id = 1))!!
    val rest = getArea("Rest", eav(1, voice = 2))!!
    assert(note.coord.y + note.area.height <= rest.coord.y)
  }

}
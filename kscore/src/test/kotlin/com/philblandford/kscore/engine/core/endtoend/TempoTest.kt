package com.philblandford.kscore.engine.core.endtoend

import assertEqual


import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.tempo.Tempo
import com.philblandford.kscore.engine.types.*
import core.representation.*
import org.junit.Test

class TempoTest : RepTest() {

  @Test
  fun testAddTempo() {
    SAE(Tempo(crotchet(), 180).toEvent(), ez(2))
    RVA("Tempo", ez(2))
  }

  @Test
  fun testMoveTempoUp() {
    SAE(Tempo(crotchet(), 180).toEvent(), ez(2))
    val old = getArea("Tempo", ez(2))!!.coord.y
    SSP(EventType.TEMPO, EventParam.HARD_START, Coord(0,-20), ez(2))
    val new = getArea("Tempo", ez(2))!!.coord.y
    assertEqual(old - 20, new)
  }

  @Test
  fun testClearTempoShift() {
    SAE(Tempo(crotchet(), 180).toEvent(), ez(2))
    val old = getArea("Tempo", ez(2))!!.coord.y
    SSP(EventType.TEMPO, EventParam.HARD_START, Coord(0,-20), ez(2))
    SSP(EventType.TEMPO, EventParam.HARD_START, null, ez(2))
    val new = getArea("Tempo", ez(2))!!.coord.y
    assertEqual(old, new)
  }

  @Test
  fun testTextLeftOfTempo() {
    SAE(Tempo(crotchet(), 180).toEvent(), ez(2))
    SAE(EventType.TEMPO_TEXT, ez(2), paramMapOf(EventParam.TEXT to "Allegro"))
    assert(isLeft("TempoText", ez(2), "Tempo", ez(2)) == true)
  }

  @Test
  fun testTempoShownOnceSelectedPartGrandStave() {
    RCD(instruments = listOf("Violin", "Piano"))
    SSP(EventType.UISTATE, EventParam.SELECTED_PART, 2, eZero())
    assertEqual(1, getAreas("Tempo").size)
  }

}
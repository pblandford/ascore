package com.philblandford.kscore.engine.core.endtoend

import assertEqual
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.duration.*

import core.representation.*
import org.junit.Test

class TieTest : RepTest() {

  @Test
  fun testAddTie() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    SAE(EventType.TIE, eav(1).copy(id = 1))
    RVA("Tie", eav(1).copy(id = 1))
  }

  @Test
  fun testAddTieToChord() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    SAE(EventType.TIE, eav(1))
    RVA("Tie", eav(1).copy(id = 1))
  }

  @Test
  fun testAddTieToChordHasWidthHeight() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    SAE(EventType.TIE, eav(1))
    val area = getArea("Tie", eav(1).copy(id = 1))?.area!!
    assert(area.width > 0)
    assert(area.height > 0)
  }

  @Test
  fun testAddTieToChordMultipleNotes() {
    SMV()
    SMV(70)
    SMV(eventAddress = eav(1, crotchet()))
    SMV(70, eventAddress = eav(1, crotchet()))
    SAE(EventType.TIE, eav(1))
    RVA("Tie", eav(1).copy(id = 1))
    RVA("Tie", eav(1).copy(id = 2))
  }

  @Test
  fun testAddTieFromTuplet() {
    SAE(EventType.TUPLET, eav(1), paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8))
    SMV(duration = quaver(), eventAddress = eav(1, Duration(1,6)))
    SMV(eventAddress = eav(1, crotchet()))
    SAE(EventType.TIE, eav(1, Duration(1, 6)))
    RVA("Tie", eav(1, Duration(1,6)).copy(id = 1))
  }

  @Test
  fun testAddTieOverhang() {
    repeat(EG().numBars) { bar ->
      repeat(4) { offset ->
        SMV(eventAddress = eav(bar + 1, crotchet().multiply(offset)))
      }
    }
    val stave2Bar = getStaveBar(2)
    SAE(EventType.TIE, eav(stave2Bar-1, minim(1)))
    val ties = getAreas("Tie")
    assertEqual(2, ties.size)
  }

  @Test
  fun testAddTieToOneNoteInChord() {
    SMV()
    SMV(69)
    SMV(eventAddress = eav(1, crotchet()))
    SMV(69, eventAddress = eav(1, crotchet()))
    SAE(EventType.TIE, eav(1).copy(id = 2))
    assertEqual(1, getAreas("Tie").size)
  }


  @Test
  fun testDeleteTie() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    SAE(EventType.TIE, eav(1).copy(id = 1))
    SDE(EventType.TIE, eav(1).copy(id = 1))
    RVNA("Tie", eav(1).copy(id = 1))
  }

  @Test
  fun testDeleteNoteTieGone() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    SAE(EventType.TIE, eav(1).copy(id = 1))
    SDE(EventType.NOTE, eav(1).copy(id = 1))
    RVNA("Tie", eav(1).copy(id = 1))
  }

  @Test
  fun testDeleteEndNoteTieGone() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    SAE(EventType.TIE, eav(1).copy(id = 1))
    SDE(EventType.NOTE, eav(1, crotchet()).copy(id = 1))
    RVNA("Tie", eav(1).copy(id = 1))
  }

  @Test
  fun testDeleteEndChordTieGone() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    SAE(EventType.TIE, eav(1).copy(id = 1))
    SDE(EventType.DURATION, eav(1, crotchet()))
    assertEqual(0, getAreas("Tie").count())
    RVNA("Tie", eav(1).copy(id = 1))
    RVNA("Tie", eav(1, crotchet()).copy(id = 1))
  }

  @Test
  fun testDeleteEndNoteRangeTieGone() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    SAE(EventType.TIE, eav(1).copy(id = 1))
    SDR(ea(1, crotchet()), ea(1, crotchet()))
    assertEqual(0, getAreas("Tie").count())
  }

  @Test
  fun testAddNoteTieToLastV2() {
    SMV(eventAddress = eav(1, dZero(), 2))
    SMV(eventAddress = eav(1, crotchet(), 2), extraParams = paramMapOf(EventParam.TIE_TO_LAST to true))
    RVA("Tie", eav(1, dZero(), 2).copy(id = 1))
  }
}
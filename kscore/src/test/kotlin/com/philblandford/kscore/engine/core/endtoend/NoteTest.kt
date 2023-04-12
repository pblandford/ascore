package com.philblandford.kscore.engine.core.endtoend

import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.eav
import com.philblandford.kscore.engine.types.paramMapOf
import com.philblandford.kscore.engine.core.representation.getArea

import org.junit.Test
import com.philblandford.kscore.engine.core.representation.scoreToRepresentation
import com.philblandford.kscore.engine.dsl.createScoreOneNote
import com.philblandford.kscore.engine.dsl.scoreAllCrotchets
import com.philblandford.kscore.engine.dsl.scoreBar2
import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.duration.quaver


import core.representation.RepTest


class NoteTest : RepTest() {
  @Test
  fun testNoteCreated() {
    val score = createScoreOneNote()
    val rep =
      scoreToRepresentation(
        score, drawableFactory
      )
    assert(rep?.getArea("Chord", eav(1)) != null)
  }

  @Test
  fun testNoteCreatedBarTwo() {
    val score = scoreBar2()
    val rep =
      scoreToRepresentation(
        score, drawableFactory
      )
    assert(rep?.getArea("Chord", eav(2)) != null)
  }

  @Test
  fun testNoteCreatedMultipleBars() {
    val score = scoreAllCrotchets(5)
    val rep =
      scoreToRepresentation(
        score, drawableFactory
      )

    (1..5).forEach { bar ->
      (0..3).forEach { beat ->
        val offset = crotchet().multiply(beat)
        assert(rep?.getArea("Chord", eav(bar, offset)) != null)
      }
    }
  }

  @Test
  fun testWholeBarRestVoice1() {
    SMV(eventAddress = eav(1, voice = 2))
    RVA("Rest", eav(1))
  }

  @Test
  fun testWholeBarRestVoice1AboveNote() {
    SMV(eventAddress = eav(1, voice = 2))
    val rest = getArea("Rest", eav(1))!!
    val note = getArea("Chord", eav(1, voice = 2))!!
    assert(rest.coord.y + rest.area.height < note.coord.y)
  }

  @Test
  fun testNoRestAfterNoteAdd() {
    SMV(eventAddress = eav(1))
    RVNA("Rest", eav(1))
    RVNA("Rest", eav(1, voice = 2))
  }

  @Test
  fun testNoteCreatedSmall() {
    SMV()
    val normal = getArea("Tadpole", eav(1).copy(id = 1))!!.area
    SMV(extraParams = paramMapOf(EventParam.IS_SMALL to true))
    val small = getArea("Tadpole", eav(1).copy(id = 1))!!.area
    assert(normal.width > small.width)
  }

  @Test
  fun testAddNoteSameYPosition() {
    SMV(72)
    SMV(73)
    RVA("Tadpole", eav(1).copy(id = 1))
    RVA("Tadpole", eav(1).copy(id = 2))
  }

  @Test
  fun testOverwriteSmallerDuration() {
    SMV(duration = quaver())
    SMV(duration = quaver(), eventAddress = eav(1, quaver()))
    SMV(duration = crotchet())
    RVNA("Tadpole", eav(1, quaver()).copy(id = 1))
  }

}
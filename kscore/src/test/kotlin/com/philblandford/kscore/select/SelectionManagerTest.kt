package com.philblandford.kscore.select

import assertEqual
import com.philblandford.kscore.api.Location
import com.philblandford.kscore.api.Rectangle
import com.philblandford.kscore.api.ScoreArea
import com.philblandford.kscore.engine.core.score.Meta
import com.philblandford.kscore.engine.dsl.dslChord
import com.philblandford.kscore.engine.dsl.note
import com.philblandford.kscore.engine.duration.chord
import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.types.*
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SelectionManagerTest {

  private lateinit var sm: SelectionManager

  @Before
  fun setUp() {
    sm = SelectionManager()
  }

  @Test
  fun testSetStartSelection() {
    sm.setStartSelect(ea(1))
    assertThat(sm.getStartSelection(), `is`(ea(1)))
  }

  @Test
  fun testSetEndSelection() {
    sm.setStartSelect(ea(1))
    sm.setEndSelect(ea(2))
    assertThat(sm.getEndSelection(), `is`(ea(2)))
  }

  @Test
  fun testSetSelectedArea() {
    sm.setSelectedArea(getAts(), listOf(getAts()))
    assertThat(sm.getSelectedArea(), `is`(getAts()))
  }

  @Test
  fun testSetSelectedAreaThenRefresh() {
    sm.setSelectedArea(getAts(), listOf(getAts()))
    sm.refreshAreas { listOf(getAts()) }
    assertThat(sm.getSelectedArea(), `is`(getAts()))
  }

  @Test
  fun testSetSelectedAreaThenRefreshOtherEventTypes() {
    val areas = getAtsMixture()
    sm.setSelectedArea(areas[1], listOf(areas[1]))
    sm.refreshAreas { areas }
    assertThat(sm.getSelectedArea(), `is`(areas[1]))
  }

  @Test
  fun testSetEndSelectionIsNullWithoutStart() {
    sm.setEndSelect(ea(2))
    assert(sm.getEndSelection() == null)
  }

  @Test
  fun testMoveSelectionLeft() {
    sm.setStartSelect(ea(2))
    sm.moveSelection { ea -> ea(ea.barNum - 1) }
    assertThat(sm.getStartSelection(), `is`(ea(1)))
  }

  @Test
  fun testMoveSelectionRight() {
    sm.setStartSelect(ea(2))
    sm.moveSelection { ea -> ea(ea.barNum + 1) }
    assertEqual(ea(3), sm.getStartSelection())
  }

  @Test
  fun testMoveSelectionLeftRange() {
    sm.setStartSelect(ea(2))
    sm.setEndSelect(ea(4))
    sm.moveSelection { ea -> ea(ea.barNum - 1) }
    assertEqual(ea(2), sm.getStartSelection())
    assertEqual(ea(3), sm.getEndSelection())
  }

  @Test
  fun testMoveSelectionRightRange() {
    sm.setStartSelect(ea(2))
    sm.setEndSelect(ea(4))
    sm.moveSelection { ea -> ea(ea.barNum + 1) }
    assertEqual(ea(2), sm.getStartSelection())
    assertEqual(ea(5), sm.getEndSelection())
  }

  @Test
  fun testCycleSelectedArea() {
    sm.setStartSelect(ea(1))
    sm.cycleArea { getAtsChords() }
    assertThat(sm.getSelectedArea()?.event?.eventType, `is`(EventType.NOTE))
  }

  @Test
  fun testCycleSelectedAreaToNote() {
    val chords = getAtsChords()
    sm.setStartSelect(ea(1))
    sm.cycleArea { chords }
    sm.cycleArea { chords }
    assertThat(sm.getSelectedArea()?.event?.eventType, `is`(EventType.NOTE))
  }

  @Test
  fun testCycleSelectedAreaToVoice2Chord() {
    val chords = getAtsChordsTwoVoices()
    sm.setStartSelect(ea(1))
    sm.cycleArea { chords }
    sm.cycleArea { chords }
    sm.cycleArea { chords }
    assertThat(sm.getSelectedArea()?.event?.eventType, `is`(EventType.NOTE))
    assertThat(sm.getSelectedArea()?.eventAddress?.voice, `is`(2))
  }

  @Test
  fun testClearSelection() {
    sm.setStartSelect(ea(1))
    sm.clearSelection()
    assertThat(sm.getStartSelection(), `is`(null as EventAddress?))
    assertThat(sm.getEndSelection(), `is`(null as EventAddress?))
  }

  @Test
  fun testClearSelectionClearsATS() {
    sm.setSelectedArea(getAts(), listOf(getAts()))
    sm.clearSelection()
    assertThat(sm.getSelectedArea(), `is`(null as AreaToShow?))
  }

  private fun getAts(): AreaToShow {
    return AreaToShow(
      ScoreArea(1, Rectangle(50, 50, 50, 50)), ea(1),
      Event(EventType.CLEF, paramMapOf(EventParam.TYPE to ClefType.TREBLE))
    )
  }



  private fun getAtsMixture(): List<AreaToShow> {
    return listOf(EventType.DURATION, EventType.TEMPO).map {
      AreaToShow(
        ScoreArea(1, Rectangle(50, 50, 50, 50)), eZero(),
        Event(it, paramMapOf(EventParam.SECTIONS to Meta()))
      )
    }
  }


  private fun getAtsChords(): List<AreaToShow> {
    val list = listOf(
      AreaToShow(
        ScoreArea(1, Rectangle(50, 50, 50, 50)), ea(1),
        dslChord {
          pitch(NoteLetter.A)
          pitch(NoteLetter.B)
          pitch(NoteLetter.C)
        }
      ),
    )
    val notes = listOf(NoteLetter.A, NoteLetter.B, NoteLetter.C).withIndex().map { iv ->
      AreaToShow(
        ScoreArea(1, Rectangle(50, 50, 50, 50)),
        ea(1).copy(id = iv.index + 1), note(iv.value, crotchet())
      )
    }
    return list + notes
  }

  private fun getAtsChordsTwoVoices(): List<AreaToShow> {
    val list = listOf(
      AreaToShow(
        ScoreArea(1, Rectangle(50, 50, 50, 50)), eav(1),
        dslChord {
          pitch(NoteLetter.A)
        }
      ),
      AreaToShow(
        ScoreArea(1, Rectangle(50, 50, 50, 50)), eav(1).copy(id = 1),
        note(NoteLetter.A, crotchet())
      ),
      AreaToShow(
        ScoreArea(1, Rectangle(50, 50, 50, 50)), eav(1, voice = 2),
        dslChord {
          pitch(NoteLetter.A)
        }
      ),
      AreaToShow(
        ScoreArea(1, Rectangle(50, 50, 50, 50)), eav(1, voice = 2).copy(id = 1),
        note(NoteLetter.A, crotchet())
      )
    )
    return list
  }
}
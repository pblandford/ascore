package com.philblandford.kscore.engine.core.areadirectory

import assertEqual
import com.philblandford.kscore.engine.types.NoteLetter
import com.philblandford.kscore.engine.types.Pitch
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.areadirectory.segment.VoiceRelation.*
import com.philblandford.kscore.engine.core.areadirectory.segment.getVoiceRelation
import com.philblandford.kscore.engine.dsl.note
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.duration.Note
import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.duration.minim
import org.junit.Test


/**
 * Created by philb on 14/09/16.
 */
class VoiceRelationDeciderTest  {

  @Test
	fun testVoiceRelationNoConflict() {
    val noteList1 = listOf(PCoord(0,0))
    val noteList2 = listOf(PCoord(0,2))

    assertEqual(NO_CONFLICT, getVoiceRelation(noteList1, noteList2))
  }

  @Test
	fun testVoiceRelationConflict() {
    val noteList1 = listOf(PCoord(0,0))
    val noteList2 = listOf(PCoord(0,1))

    assertEqual(CONFLICT, getVoiceRelation(noteList1, noteList2))
  }

  @Test
	fun testVoiceRelationOneListEmpty() {
    val noteList1 = listOf(PCoord(0,0))
    val noteList2 = listOf<Note>()

    assertEqual(NO_CONFLICT, getVoiceRelation(noteList1, noteList2))
  }

  @Test
	fun testVoiceRelationIntertwined() {
    val noteList1 = listOf(PCoord(0,4))
    val noteList2 = listOf(PCoord(0,1))

    assertEqual(INTERTWINED, getVoiceRelation(noteList1, noteList2))
  }

  @Test
	fun testVoiceRelationConflictIntertwined() {
    val noteList1 = listOf(PCoord(0,4))
    val noteList2 = listOf(PCoord(0,3))
    assertEqual(CONFLICT_INTERTWINED, getVoiceRelation(noteList1, noteList2))
  }

  @Test
	fun testVoiceRelationConflictIntertwinedIfSameY() {
    val noteList1 = listOf(PCoord(0,4))
    val noteList2 = listOf(PCoord(0,1), PCoord(0,4))
    assertEqual(CONFLICT_INTERTWINED, getVoiceRelation(noteList1, noteList2))
  }

  @Test
	fun testVoiceRelationConflictIntertwinedTwoNotesV2() {
    val noteList1 = listOf(PCoord(0,6))
    val noteList2 = listOf(PCoord(0,4), PCoord(0,7))
    assertEqual(CONFLICT_INTERTWINED, getVoiceRelation(noteList1, noteList2))
  }

  @Test
	fun testVoiceRelationNoConflictIfVoiceMinus1() {
    val noteList1 = listOf(PCoord(0,2))
    val noteList2 = listOf(PCoord(0,2), PCoord(-1,3))

    assertEqual(NO_CONFLICT, getVoiceRelation(noteList1, noteList2))
  }

  @Test
	fun testVoiceRelationNoConflictSameNote() {
    val noteList1 = listOf(PCoord(0,0))
    val noteList2 = listOf(PCoord(0,0))

    assertEqual(NO_CONFLICT, getVoiceRelation(noteList1, noteList2))
  }

  @Test
	fun testVoiceRelationConflictSameNoteDifferentNoteHead() {
    val noteList1 = listOf(PCoord(0,0, crotchet()))
    val noteList2 = listOf(PCoord(0,0, minim()))

    assertEqual(CONFLICT, getVoiceRelation(noteList1, noteList2))
  }

	private fun PCoord(x:Int, y:Int, duration:Duration = crotchet()): Note {
    return com.philblandford.kscore.engine.duration.note(note(Pitch(NoteLetter.D), duration))!!.
      copy(position = Coord(x,y))
  }
}

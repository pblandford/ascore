package com.philblandford.kscore.engine.newadder.util

import assertEqual
import com.philblandford.kscore.engine.core.area.Coord

import com.philblandford.kscore.engine.duration.Note
import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.scorefunction.ScoreTest
import com.philblandford.kscore.engine.types.*
import org.junit.Test

class VoiceMapUtilTest : ScoreTest() {

  @Test
  fun testGetNotes() {
    SMV()
    val vm = EG().getVoiceMap(eav(1))!!
    val expected = mapOf(
      eZero().copy(id = 1) to Note(
        crotchet(),
        Pitch(NoteLetter.C, Accidental.NATURAL, 5),
        position = Coord(0, 3)
      )
    )
    assertEqual(expected, vm.getNotes())
  }
}
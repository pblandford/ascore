package com.philblandford.kscore.engine.newadder.util

import assertEqual

import com.philblandford.kscore.engine.duration.semibreve
import com.philblandford.kscore.engine.newadder.rightOrThrow

import com.philblandford.kscore.engine.scorefunction.ScoreTest
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.StaveId
import com.philblandford.kscore.engine.types.eav
import org.junit.Test

class StaveUtilTest : ScoreTest() {

  @Test
  fun testSetTies() {
    SMV(duration = semibreve())
    SMV(duration = semibreve(), eventAddress = eav(2))
    SSP(EventType.NOTE, EventParam.IS_START_TIE, true, eav(1).copy(id = 1))
    var stave = EG().getStave(StaveId(1,1))!!
    stave = stave.setTies(EG()).rightOrThrow()
    val note =  stave.getBar(2)!!.voiceMaps[0].getNotes().toList().first().second
    assert(note.isEndTie)
    assertEqual(semibreve(), note.endTie)
  }
}
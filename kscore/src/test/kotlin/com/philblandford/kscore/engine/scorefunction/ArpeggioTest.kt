package com.philblandford.kscore.engine.scorefunction

import com.philblandford.kscore.engine.types.ArpeggioType
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.eav
import com.philblandford.kscore.engine.newadder.subadders.ChordDecoration
import org.junit.Test

class ArpeggioTest : ScoreTest() {

  @Test
  fun testAddArpeggio() {
    SMV()
    SAE(EventType.ARPEGGIO, eav(1))
    SVP(
      EventType.DURATION,
      EventParam.ARPEGGIO,
      ChordDecoration(items = listOf(ArpeggioType.NORMAL), up = false)
    )
  }

  @Test
  fun testDeleteArpeggio() {
    SMV()
    SAE(EventType.ARPEGGIO, eav(1))
    SDE(EventType.ARPEGGIO, eav(1))
    SVNP(EventType.DURATION, EventParam.ARPEGGIO, eav(1))
  }
}
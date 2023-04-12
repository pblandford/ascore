package com.philblandford.kscore.engine.scorefunction

import com.philblandford.kscore.engine.types.*


import org.junit.Test

class NavigationTest : ScoreTest() {

  @Test
  fun testAddFermata() {
    SMV()
    SAE(EventType.NAVIGATION, ea(1), params = paramMapOf(EventParam.TYPE to NavigationType.CODA))
    SVP(EventType.NAVIGATION, EventParam.TYPE, NavigationType.CODA, ez(1))
  }

}
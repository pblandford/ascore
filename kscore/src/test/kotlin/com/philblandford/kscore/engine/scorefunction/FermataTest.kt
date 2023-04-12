package com.philblandford.kscore.engine.scorefunction

import assertEqual
import com.philblandford.kscore.engine.types.*


import com.philblandford.kscore.engine.duration.minim
import org.junit.Test

class FermataTest : ScoreTest() {

  @Test
  fun testAddFermata() {
    SMV()
    SAE(EventType.FERMATA, ea(1), params = paramMapOf(EventParam.TYPE to FermataType.NORMAL))
    SVP(EventType.FERMATA, EventParam.TYPE, FermataType.NORMAL, ez(1))
  }

  @Test
  fun testAddFermataIgnoreVoice() {
    SMV()
    SAE(EventType.FERMATA, ea(1), params = paramMapOf(EventParam.TYPE to FermataType.NORMAL))
    SVP(EventType.FERMATA, EventParam.TYPE, FermataType.NORMAL, eav(1))
  }

  @Test
  fun testAddFermataMidBar() {
    SMV()
    SAE(EventType.FERMATA, ea(1, minim()), params = paramMapOf(EventParam.TYPE to FermataType.NORMAL))
    SVP(EventType.FERMATA, EventParam.TYPE, FermataType.NORMAL, ez(1, minim()))
  }

  @Test
  fun testAddFermataMidBarByEz() {
    SMV()
    SAE(EventType.FERMATA, ez(1, minim()), params = paramMapOf(EventParam.TYPE to FermataType.NORMAL))
    SVP(EventType.FERMATA, EventParam.TYPE, FermataType.NORMAL, ez(1, minim()))
  }

  @Test
  fun testAdTwoFermatas() {
    SMV()
    SAE(EventType.FERMATA, ea(1), params = paramMapOf(EventParam.TYPE to FermataType.NORMAL))
    SAE(EventType.FERMATA, ea(1, minim()), params = paramMapOf(EventParam.TYPE to FermataType.NORMAL))
    assertEqual(2, EG().getEvents(EventType.FERMATA)?.size)
    SVP(EventType.FERMATA, EventParam.TYPE, FermataType.NORMAL, ez(1))
    SVP(EventType.FERMATA, EventParam.TYPE, FermataType.NORMAL, ez(1, minim()))
  }

  @Test
  fun testAdTwoFermatasByEz() {
    SMV()
    SAE(EventType.FERMATA, ez(1), params = paramMapOf(EventParam.TYPE to FermataType.NORMAL))
    SAE(EventType.FERMATA, ez(1, minim()), params = paramMapOf(EventParam.TYPE to FermataType.NORMAL))
    SVP(EventType.FERMATA, EventParam.TYPE, FermataType.NORMAL, ez(1))
    SVP(EventType.FERMATA, EventParam.TYPE, FermataType.NORMAL, ez(1, minim()))
  }



}
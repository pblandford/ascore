package com.philblandford.kscore.engine.core.endtoend

import com.philblandford.kscore.engine.types.*
import core.representation.RepTest
import org.junit.Test

class HideStaveTest : RepTest() {

  @Test
  fun testHideStaveSecondEmpty() {
    RCD(instruments = listOf("Violin", "Viola"), bars = 100)
    val page = REP().pages[1]

    SMV(72, eventAddress = eav(page.geography.startBar))
    SSO(EventParam.OPTION_HIDE_EMPTY_STAVES, true)
    val newPage = REP().pages[1]
    assert(newPage.base.findByTag("Stave").any { it.key.eventAddress == eas(newPage.geography.startBar, 1,1) })
    assert(!newPage.base.findByTag("Stave").any { it.key.eventAddress == eas(newPage.geography.startBar, 2,1) })
  }


  @Test
  fun testHideStaveFirstEmpty() {
    RCD(instruments = listOf("Violin", "Viola"), bars = 100)
    val page = REP().pages[1]

    SMV(72, eventAddress = easv(page.geography.startBar, 2, 1))
    SSO(EventParam.OPTION_HIDE_EMPTY_STAVES, true)
    val newPage = REP().pages[1]
    assert(newPage.base.findByTag("Stave").any { it.key.eventAddress == eas(newPage.geography.startBar, 2,1) })
    assert(!newPage.base.findByTag("Stave").any { it.key.eventAddress == eas(newPage.geography.startBar, 1,1) })
  }


  @Test
  fun testHideStaveFirstEmptyVoltaShows() {
    RCD(instruments = listOf("Violin", "Viola"), bars = 100)
    val page = REP().pages[1]
    val page2bar = page.geography.startBar

    SMV(72, eventAddress = easv(page2bar, 2, 1))
    SAE(EventType.VOLTA, ez(page2bar), paramMapOf(EventParam.NUMBER to 1, EventParam.END to ez(page2bar+1)))
    RVA("Volta", ez(page2bar))
    SSO(EventParam.OPTION_HIDE_EMPTY_STAVES, true)
    RVA("Volta", ez(page2bar))
  }
}
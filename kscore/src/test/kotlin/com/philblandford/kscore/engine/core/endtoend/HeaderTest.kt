package com.philblandford.kscore.engine.core.endtoend

import assertEqual
import com.philblandford.kscore.engine.types.*


import com.philblandford.kscore.engine.duration.minim
import com.philblandford.kscore.engine.pitch.KeySignature
import com.philblandford.kscore.engine.time.TimeSignature

import core.representation.RepTest
import org.junit.Test

class HeaderTest : RepTest() {

  @Test
  fun testHeaderCreated() {
    RVA("Header", ea(1))
  }

  @Test
  fun testHeaderHasClef() {
    RCD()
    val header = getArea("Header", ea(1))!!
    assert(header.area.findByTag("Clef").size == 1)
  }

  @Test
  fun testHeaderClefCorrect() {
    val header = getArea("Header", ea(1))!!
    assert(header.area.findByTag("Clef").toList().first().second.event?.subType == ClefType.TREBLE)
  }

  @Test
  fun testHeaderClefCorrectSecondStave() {
    val start = getStaveBar(1)
    val header = getArea("Header", ea(start))!!
    assert(header.area.findByTag("Clef").toList().first().second.event?.subType == ClefType.TREBLE)
  }

  @Test
  fun testHeaderClefCorrectChangeFirstBar() {
    SAE(EventType.CLEF, ea(1, minim()), paramMapOf(EventParam.TYPE to ClefType.BASS))
    val header = getArea("Header", ea(1))!!
    assertEqual(
      ClefType.TREBLE,
      header.area.findByTag("Clef").toList().first().second.event?.subType
    )
  }

  @Test
  fun testHeaderClefCorrectSecondStaveClefChange() {
    var start = getStaveBar(1)
    SAE(EventType.CLEF, ea(start - 1), paramMapOf(EventParam.TYPE to ClefType.BASS))
    start = getStaveBar(1)
    checkClef(start, ClefType.BASS)
  }

  @Test
  fun testHeaderClefCorrectSecondStaveSecondBarClefChange() {
    var start = getStaveBar(1)
    SAE(EventType.CLEF, ea(start + 1), paramMapOf(EventParam.TYPE to ClefType.BASS))
    start = getStaveBar(1)
    checkClef(start, ClefType.TREBLE)
  }

  @Test
  fun testHeaderHasKeySignature() {
    val header = getArea("Header", ea(1))!!
    assert(header.area.findByTag("KeySignature").size == 1)
  }

  @Test
  fun testHeaderKeySignatureCorrect() {
    SAE(KeySignature(2).toEvent(), ea(1))
    val header = getArea("Header", ea(1))!!
    assertEqual(
      2,
      header.area.findByTag("KeySignature").toList().first().second.event?.getParam(EventParam.SHARPS)
    )
  }

  @Test
  fun testHeaderKeySignatureCorrectSecondStave() {
    SAE(KeySignature(2).toEvent(), ea(1))
    val start = getStaveBar(1)
    val header = getArea("Header", ea(start))!!
    assertEqual(
      2,
      header.area.findByTag("KeySignature").toList().first().second.event?.getParam(EventParam.SHARPS)
    )
  }

  @Test
  fun testHeaderKeySignatureCorrectSecondStaveKeySignatureChange() {
    var start = getStaveBar(1)
    SAE(KeySignature(2).toEvent(), ea(start - 1))
    start = getStaveBar(1)
    val header = getArea("Header", ea(start))!!
    assertEqual(
      2,
      header.area.findByTag("KeySignature").toList().first().second.event?.getParam(EventParam.SHARPS)
    )
  }

  @Test
  fun testHeaderNoKeySignaturePercussion() {
    RCD(instruments = listOf("Bass Drum 1"))
    val header = getArea("Header", ea(1))!!
    assert(header.area.findByTag("KeySignature").isEmpty())
  }

  @Test
  fun testHeaderHasTimeSignature() {
    val header = getArea("Header", ea(1))!!
    assert(header.area.findByTag("TimeSignature").size == 1)
  }

  @Test
  fun testHeaderTimeSignatureCorrect() {
    SAE(TimeSignature(3, 4).toEvent(), ea(2))
    val header = getArea("Header", ea(1))!!
    assertEqual(
      4,
      header.area.findByTag("TimeSignature").toList().first().second.event?.getParam(EventParam.NUMERATOR)
    )
  }

  @Test
  fun testHeaderHasNoTimeSignature() {
    val header = getArea("Header", ea(getStaveBar(1)))!!
    assert(header.area.findByTag("TimeSignature").isEmpty())
  }

  @Test
  fun testHeadersAligned() {
    RCD(instruments = listOf("Trumpet", "Violin"))
    val trumpetTime = getArea("TimeSignature", ea(1))!!
    val violinTime = getArea("TimeSignature", eas(1, 2, 1))!!
    assertEqual(trumpetTime.coord.x, violinTime.coord.x)
  }

  @Test
  fun testKeySignatureHeaderTransposing() {
    RCD(instruments = listOf("Violin", "Trumpet"), ks = 2)
    val header = getArea("Header", eas(1, 2, 1))!!
    assertEqual(
      4,
      header.area.findByTagSingle("KeySignature")?.event?.getInt(EventParam.SHARPS)
    )
  }


  @Test
  fun testSetTransposingInstrumentOptionHeaderKsChanged() {
    RCD(instruments = listOf("Violin", "Trumpet"), ks = 2)
    SSO(EventParam.OPTION_SHOW_TRANSPOSE_CONCERT, true)
    val header = getArea("Header", eas(1, 2, 1))!!
    assertEqual(
      2,
      header.area.findByTagSingle("KeySignature")?.event?.getInt(EventParam.SHARPS)
    )
  }


  private fun checkClef(bar: Int, clefType: ClefType) {
    val header = getArea("Header", ea(bar))!!
    assertEqual(clefType, header.area.findByTag("Clef").toList().first().second.event?.subType)
  }
}
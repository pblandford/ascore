package com.philblandford.kscore.engine.scorefunction

import assertEqual
import com.philblandford.kscore.engine.core.LayoutDescriptor
import com.philblandford.kscore.engine.core.getLayoutDescriptor
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.core.representation.PAGE_WIDTH
import org.junit.Test

class LayoutTest : ScoreTest() {
  @Test
  fun testSetPageWidth() {
    SSO(EventParam.LAYOUT_PAGE_WIDTH, 5000)
    assertEqual(5000, getLayout().pageWidth)
  }

  @Test
  fun testSelectedPartIgnoresTopLevelPageWidth() {
    SCD(instruments = listOf("Violin", "Viola"))
    SSO(EventParam.LAYOUT_PAGE_WIDTH, 5000)
    sc.setSelectedPart(1)
    assertEqual(PAGE_WIDTH, getLayout().pageWidth)
  }

  @Test
  fun testSetPageWidthSelectedPart() {
    SCD(instruments = listOf("Violin", "Viola"))
    sc.setSelectedPart(1)
    SSO(EventParam.LAYOUT_PAGE_WIDTH, 5000)
    assertEqual(5000, getLayout().pageWidth)
  }

  @Test
  fun testSetPageWidthSelectedPartDoesntEffectScoreLevel() {
    SCD(instruments = listOf("Violin", "Viola"))
    val original = getLayout().pageWidth
    sc.setSelectedPart(1)
    SSO(EventParam.LAYOUT_PAGE_WIDTH, 5000)
    sc.setSelectedPart(0)
    assertEqual(original, getLayout().pageWidth)
  }

  private fun getLayout(): LayoutDescriptor {
    return SCORE().let { getLayoutDescriptor(it) } ?: LayoutDescriptor()
  }
}
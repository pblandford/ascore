package com.philblandford.kscore.engine.core.score

import assertEqual


import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.update.ScoreDiff
import org.junit.Test
import com.philblandford.kscore.engine.scorefunction.ScoreTest
import com.philblandford.kscore.engine.update.diff

class ScoreUpdateTest : ScoreTest() {

  @Test
  fun testScoreUpdate() {
    val diff = doChange { SMV() }
    assertEqual(listOf(ea(1), ea(2)).toList(), diff.changedBars.toList())
  }

  @Test
  fun testScoreUpdateSecondBar() {
    val diff = doChange { SMV(eventAddress = ea(2)) }
    assertEqual(listOf(ea(1), ea(2), ea(3)).toList(), diff.changedBars.toList())
  }

  @Test
  fun testScoreUpdateTwoBars() {
    val diff = doChange {
      SMV()
      SMV(eventAddress = ea(2))
    }
    assertEqual(listOf(ea(1), ea(2), ea(3)).toList(), diff.changedBars.toList())
  }

  @Test
  fun testScoreUpdateStaveEventNoBarChanges() {
    SMV()
    val diff = doChange {
      SAE(EventType.EXPRESSION_TEXT, params = paramMapOf(EventParam.TEXT to "SEOIWUOEI"))
    }
    assertEqual(listOf<EventAddress>(), diff.changedBars.toList())
  }

  @Test
  fun testScoreUpdateSystemEventLineChange() {
    val diff = doChange {
      SAE(EventType.TEMPO_TEXT, ez(1), paramMapOf(EventParam.TEXT to "WEWUIO"))
    }
    assertEqual(listOf(1), diff.changedLines.toList())
  }

  @Test
  fun testScoreUpdateLineEventLineChange() {
    val diff = doChange {
      SAE(
        EventType.WEDGE, ea(1), paramMapOf(
          EventParam.END to ea(2),
          EventParam.TYPE to WedgeType.CRESCENDO
        )
      )
    }
    assertEqual(listOf(1, 2).toList(), diff.changedLines.toList())
  }

  @Test
  fun testScoreUpdateAddBar() {
    val diff = doChange {
      SAE(EventType.BAR, ez(2), paramMapOf(EventParam.NUMBER to 1))
    }
    assert(diff.recreate)
  }

  @Test
  fun testScoreUpdateSetOption() {
    val diff = doChange {
      SSO(EventParam.OPTION_BAR_NUMBERING, 4)
    }
    assert(diff.createParts)
  }

  private fun doChange(change: () -> Unit): ScoreDiff {
    val old = SCORE()
    change()
    val new = SCORE()
    return old.diff(new)
  }

}
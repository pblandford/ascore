package com.philblandford.kscore.engine.creation

import com.philblandford.kscore.api.NewScoreDescriptor
import com.philblandford.kscore.api.defaultInstrument
import com.philblandford.kscore.engine.core.representation.TITLE_TEXT_SIZE
import com.philblandford.kscore.engine.core.score.Meta
import com.philblandford.kscore.engine.scorefunction.ScoreTest
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.MetaType
import com.philblandford.kscore.engine.types.eZero
import com.philblandford.kscore.engine.types.ea
import com.philblandford.kscore.engine.types.eav
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Test

class ScoreCreatorTest : ScoreTest() {

  private val creator = ScoreCreator(instrumentGetter)

  @Test
  fun testCreateDefault() {
    val score = creator.createDefault()
    assertThat(score.parts.size, `is`(1))
  }

  @Test
  fun testCreateTitle() {
    val score = creator.createScore(NewScoreDescriptor(meta = Meta().setText(MetaType.TITLE, "The Title")))
    assertThat(score.getTitle(), `is`("The Title"))
  }

  @Test
  fun testCreateTitleTextSizeCorrect() {
    val score = creator.createScore(NewScoreDescriptor(meta = Meta().setText(MetaType.TITLE, "The Title")))
    val size = score.getParam<Int>(EventType.TITLE, EventParam.TEXT_SIZE, eZero())
    assertThat(size, `is`(TITLE_TEXT_SIZE))
  }

  @Test
  fun testCreateScoreZeroBars() {
    val score = creator.createScore(NewScoreDescriptor(listOf(defaultInstrument()), numBars = 0))
    assertThat(score.numBars, `is`(0))
  }

  @Test
  fun testCreateScoreUpbeatBar() {
    val score = creator.createScore(NewScoreDescriptor(listOf(defaultInstrument()), numBars = 4,
      upBeat = TimeSignature(1,4), upbeatEnabled = true))
    assertEquals(TimeSignature(1,4, hidden = true), score.getVoiceMap(eav(1))?.timeSignature)
  }
}
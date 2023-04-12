package core.score

import TestInstrumentGetter
import com.philblandford.kscore.engine.types.*
import assertEqual
import com.philblandford.kscore.engine.core.score.Part
import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.duration.crotchet
import org.junit.Test
import com.philblandford.kscore.engine.scorefunction.ScoreTest

class ScoreBasicTest : ScoreTest() {
  @Test
  fun testCreateScore() {
    val score = Score.create(TestInstrumentGetter(),1)
    assertEqual(1, score.parts.count())
    assertEqual(1, score.parts.first().staves.first().bars.count())
    assertEqual(1, score.parts.first().staves.first().bars.first().voiceMaps.count())
  }

  @Test
  fun testScoreHasKeySignature() {
    val score = Score.create(TestInstrumentGetter(),1)
    assertEqual(0, score.getEvent(EventType.KEY_SIGNATURE, ez(1))?.params?.g(EventParam.SHARPS))
  }


  @Test
  fun testGetBarEvents() {
    SCD()
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    val events = sc.currentScore.value!!.getEvents(EventType.DURATION, eav(1))
    assertEqual(3, events?.size)
  }

  @Test
  fun testGetParts() {
    val score = Score(listOf(Part(), Part()))
    assertEqual(2, score.getEvents(EventType.PART)?.count())
  }
}
package com.philblandford.kscore.engine.newadder

import assertEqual
import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.core.score.ScoreLevelType
import com.philblandford.kscore.engine.scorefunction.ScoreTest
import com.philblandford.kscore.engine.types.*
import org.junit.Test

class NewAdderTest : ScoreTest() {

  @Test
  fun testAddEvent() {
    val score = SCORE()
    val newScore = TestAdder().addEvent(
      score,
      EventType.LAYOUT,
      paramMapOf(EventParam.LAYOUT_PAGE_HEIGHT to 500),
      eZero()
    ).rightOrThrow()
    assertEqual(
      500,
      newScore.getParam(EventType.LAYOUT, EventParam.LAYOUT_PAGE_HEIGHT, eZero())
    )
  }

  @Test
  fun testSetEvent() {
    val score = SCORE()
    var newScore = TestAdder().addEvent(
      score,
      EventType.LAYOUT,
      paramMapOf(EventParam.LAYOUT_PAGE_HEIGHT to 500),
      eZero()
    ).then {
      TestAdder().setParam(
        it,
        EventType.LAYOUT,
        EventParam.LAYOUT_PAGE_HEIGHT,
        800,
        eZero()
      )
    }.rightOrThrow()
    assertEqual(
      800,
      newScore.getParam(EventType.LAYOUT, EventParam.LAYOUT_PAGE_HEIGHT, eZero())
    )
  }


  @Test
  fun testSetEventNoEvent() {
    val score = SCORE()
    val newScore = TestAdder().setParam(
      score,
      EventType.TEST_SCORE_EVENT,
      EventParam.LAYOUT_PAGE_HEIGHT,
      800,
      eZero()
    ).rightOrThrow()
    assertEqual(
      800, newScore.getParam(
        EventType.TEST_SCORE_EVENT, EventParam.LAYOUT_PAGE_HEIGHT, eZero()
      )
    )
  }

  @Test
  fun testAddEventPartLevel() {
    val score = SCORE()
    val newScore = TestAdder().addEvent(
      score,
      EventType.TEST_PART_EVENT,
      paramMapOf(),
      eas(2, 1, 0)
    ).rightOrThrow()
    assert(
      newScore.getEvent(EventType.TEST_PART_EVENT, eas(2, 1, 0)) != null
    )
  }


  @Test
  fun testSetEventPartLevel() {
    val score = SCORE()
    val newScore = TestAdder().addEvent(
      score,
      EventType.TEST_PART_EVENT,
      paramMapOf(EventParam.LAYOUT_PAGE_HEIGHT to 500),
      eas(1, 1, 0)
    ).then { TestAdder().setParam(
        it,
        EventType.TEST_PART_EVENT,
        EventParam.LAYOUT_PAGE_HEIGHT,
        800,
        eas(1, 1, 0)
      )
    }.rightOrThrow()
    assertEqual(
      800,
      newScore.getParam(
        EventType.TEST_PART_EVENT,
        EventParam.LAYOUT_PAGE_HEIGHT,
        eas(1, 1, 0)
      )
    )
  }

  @Test
  fun testAddEventStaveLevel() {
    val score = SCORE()
    val newScore = TestAdder().addEvent(
      score,
      EventType.TEST_STAVE_EVENT,
      paramMapOf(),
      eas(2, 1, 1)
    ).rightOrThrow()
    assert(
      newScore.getEvent(EventType.TEST_STAVE_EVENT, eas(2, 1, 1)) != null
    )
  }

  @Test
  fun testAddEventBarLevel() {
    val score = SCORE()
    val newScore = TestAdder().addEvent(
      score,
      EventType.PAUSE,
      paramMapOf(),
      eav(2)
    ).rightOrThrow()
    assert(
      newScore.getEvent(EventType.PAUSE, eav(2)) != null
    )
  }

  @Test
  fun testAddEventVoiceLevel() {
    val score = SCORE()
    val newScore = TestAdder().addEvent(
      score,
      EventType.TEST_VOICE_EVENT,
      paramMapOf(),
      eav(2)
    ).rightOrThrow()
    assert(
      newScore.getEvent( EventType.TEST_VOICE_EVENT, eav(2)) != null
    )
  }

  @Test
  fun testAddEventMultipleDestinations() {
    val score = SCORE()
    val newScore = TestAdder().addEvent(
      score,
      EventType.TEST_PART_EVENT,
      paramMapOf(),
      eas(2, 0, 0)
    ).rightOrThrow()
    assert(
      newScore.getEvent(EventType.TEST_PART_EVENT, eas(2, 0, 0)) != null
    )
  }
  
  @Test
  fun testAddEventCustomAdder() {
    val score = SCORE()
    val adder = object : TestAdder() {
      override fun getDestination(eventType: EventType): EventDestination? {
        return EventDestination(listOf(ScoreLevelType.SCORE), TestScoreEventAdder)
      }
    }
    val newScore = adder.addEvent(
      score,
      EventType.TEST_SCORE_EVENT,
      paramMapOf(EventParam.LAYOUT_PAGE_HEIGHT to 500),
      eZero()
    ).rightOrThrow()
    assertEqual(
      500,
      newScore.getParam( EventType.TEST_SCORE_EVENT, EventParam.LAYOUT_PAGE_HEIGHT, eZero())
    )
    assertEqual(
      "TestText",
      newScore.getParam( EventType.TEST_SCORE_EVENT, EventParam.TEXT, eZero())
    )
  }

  private open class TestAdder : NewEventAdderIf {

    override fun getDestination(eventType: EventType): EventDestination? {
      return testDestinations[eventType]
    }
  }


  @Test
  fun testDeleteEvent() {
    val score = SCORE()
    var newScore = TestAdder().addEvent(
      score,
      EventType.TEST_SCORE_EVENT,
      paramMapOf(EventParam.LAYOUT_PAGE_HEIGHT to 500),
      eZero()
    ).rightOrThrow()
    newScore = TestAdder().deleteEvent(newScore, EventType.TEST_SCORE_EVENT, paramMapOf(), eZero()).rightOrThrow()
    assert(newScore.getEvent(EventType.TEST_SCORE_EVENT, eZero()) == null)
  }


  internal object TestScoreEventAdder : NewSubAdder {
    override fun addEvent(
      score: Score,
      destination: EventDestination,
      eventType: EventType,
      params: ParamMap,
      eventAddress: EventAddress
    ): ScoreResult {
      val newParams = params.plus(EventParam.TEXT to "TestText")
      return super.addEvent(score, destination, eventType, newParams, eventAddress)
    }
  }
}

val testDestinations = mapOf(
  EventType.LAYOUT to EventDestination(listOf(ScoreLevelType.SCORE)),
  EventType.PAUSE to EventDestination(listOf(ScoreLevelType.BAR)),
  EventType.TEST_SCORE_EVENT to EventDestination(listOf(ScoreLevelType.SCORE),
    NewAdderTest.TestScoreEventAdder
  ),
  EventType.TEST_PART_EVENT to EventDestination(
    listOf(
      ScoreLevelType.PART,
      ScoreLevelType.SCORE
    )
  ),
  EventType.TEST_STAVE_EVENT to EventDestination(listOf(ScoreLevelType.STAVE)),
  EventType.TEST_VOICE_EVENT to EventDestination(listOf(ScoreLevelType.VOICEMAP))
)


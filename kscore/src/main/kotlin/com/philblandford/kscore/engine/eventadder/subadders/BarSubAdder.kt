package com.philblandford.kscore.engine.eventadder.subadders

import com.philblandford.kscore.engine.core.score.*
import com.philblandford.kscore.engine.map.EventAdder
import com.philblandford.kscore.engine.eventadder.EventDestination
import com.philblandford.kscore.engine.eventadder.Right
import com.philblandford.kscore.engine.eventadder.ScoreResult
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.types.*
import kotlin.math.abs
import kotlin.math.min

private val retainBar1 = setOf(
  EventType.KEY_SIGNATURE, EventType.TIME_SIGNATURE,
  EventType.INSTRUMENT, EventType.CLEF, EventType.TEMPO
)

object BarSubAdder : com.philblandford.kscore.engine.eventadder.BaseSubAdder {
  override fun addEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {
    val num = params.g<Int>(EventParam.NUMBER) ?: 1
    val from = if (params.isTrue(EventParam.AFTER)) eventAddress.barNum + 1 else eventAddress.barNum
    val newScore = score.shiftBars(from, num)
    return Right(newScore)
  }

  override fun deleteEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {
    val num = params.g<Int>(EventParam.NUMBER) ?: 1
    val newScore = score.shiftBars(eventAddress.barNum, -num)
    return Right(newScore)
  }

  override fun deleteEventRange(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    eventAddress: EventAddress,
    endAddress: EventAddress
  ): ScoreResult {
    return deleteEvent(
      score,
      destination,
      eventType,
      paramMapOf(EventParam.NUMBER to endAddress.barNum - eventAddress.barNum + 1),
      eventAddress
    )
  }

  private fun Score.shiftBars(from: BarNum, number: Int): Score {
    val timeSignature = getTimeSignature(ez(from)) ?: TimeSignature(4, 4)
    val newParts = parts.map { it.shiftBars(from, number, timeSignature) }
    val em = eventMap.shiftBars(from, number)
    return Score(newParts, em, beamDirectory)
  }

  private fun Part.shiftBars(from: BarNum, number: Int, timeSignature: TimeSignature): Part {
    val newStaves = staves.map { it.shiftBars(from, number, timeSignature) }
    val em = eventMap.shiftBars(from, number)
    return Part(newStaves, em)
  }

  private fun Stave.shiftBars(from: BarNum, number: Int, timeSignature: TimeSignature): Stave {
    val newStave = if (number > 0) {
      addBars(from-1, number, timeSignature)
    } else {
      deleteBars(from, -number)
    }
    val em = eventMap.shiftBars(from, number)
    return Stave(newStave.bars, em)
  }

  private fun Stave.addBars(from: BarNum, number: Int, timeSignature: TimeSignature): Stave {
    val newBars = (1..number).map { Bar(timeSignature) }
    val allBars = bars.take(from).plus(newBars).plus(bars.takeLast(bars.size - from))
    return Stave(allBars, eventMap)
  }

  private fun Stave.deleteBars(from: BarNum, number: Int): Stave {
    val toDelete = if (from + number - 1 >= bars.size) {
      bars.size - from + 1
    } else {
      number
    }
    if (toDelete >= bars.size) {
      return this
    }
    val allBars = bars.take(from - 1).plus(bars.takeLast(bars.size - (from + toDelete - 1)))

    return Stave(allBars, eventMap)
  }

  private fun EventMap.shiftBars(from: BarNum, number: Int): EventMap {
    val em = if (number < 0) {
      deleteRange(ez(from), ez((from - number) - 1)) { et, ea ->
        (ea.barNum == 1 && retainBar1.contains(et))
      }
    } else this
    return em.shiftEvents(
      from,
      number
    ) { !(it.eventAddress.barNum == 1 && retainBar1.contains(it.eventType)) }
  }

}

object BarAdderLevel : EventAdder {
  override fun addEvent(
    happening: Happening,
    scoreLevel: ScoreLevel,
    scoreQuery: ScoreQuery?
  ): EventChangeReturn? {
    return happening.event.getParam<Int>(EventParam.NUMBER)?.let { num ->
      val newMap = scoreLevel.eventMap.shiftEvents(
        happening.eventAddress.barNum, num
      ) { emk -> !(retainBar1.contains(emk.eventType) && emk.eventAddress.barNum == 1) }
      EventChangeReturn(newMap, scoreLevel.subLevels, passDown = listOf(happening))
    }
  }

  override fun deleteEvent(
    happening: Happening,
    scoreLevel: ScoreLevel,
    scoreQuery: ScoreQuery?
  ): EventChangeReturn? {

    if ((scoreQuery?.numBars ?: 0) <= 1) {
      return null
    }

    return getNumBars(happening)?.let { num ->
      val newMap = scoreLevel.eventMap.shiftEvents(
        happening.eventAddress.barNum, -num
      ) { emk -> !(retainBar1.contains(emk.eventType) && emk.eventAddress.barNum == 1) }
      EventChangeReturn(
        newMap, scoreLevel.subLevels, passDown = listOf(
          happening.copy(
            event = happening.event.addParam(EventParam.NUMBER to num)
          )
        )
      )
    }
  }

  private fun getNumBars(happening: Happening): Int? {
    return happening.event.getParam<Int>(EventParam.NUMBER) ?: run {
      happening.endAddress?.let {
        abs(it.barNum - happening.eventAddress.barNum) + 1
      }
    }
  }
}

object BarAdderScore : EventAdder {
  override fun addEvent(
    happening: Happening,
    scoreLevel: ScoreLevel,
    scoreQuery: ScoreQuery?
  ): EventChangeReturn? {

    val modified = if (happening.event.isTrue(EventParam.AFTER)) {
      happening.copy(eventAddress = happening.eventAddress.inc())
    } else {
      happening
    }
    return BarAdderLevel.addEvent(modified, scoreLevel, scoreQuery)?.let { ecr ->

      val wildHappening = modified.copy(
        eventAddress = modified.eventAddress.copy(
          staveId = StaveId(
            INT_WILD, INT_WILD
          )
        )
      )
      EventChangeReturn(ecr.newMap, passDown = listOf(wildHappening))
    }
  }

  override fun deleteEvent(
    happening: Happening,
    scoreLevel: ScoreLevel,
    scoreQuery: ScoreQuery?
  ): EventChangeReturn? {


    return BarAdderLevel.deleteEvent(happening, scoreLevel, scoreQuery)?.let { ecr ->

      val wildHappening = happening.copy(
        eventAddress = happening.eventAddress.copy(
          staveId = StaveId(
            INT_WILD, INT_WILD
          )
        )
      )
      EventChangeReturn(ecr.newMap, passDown = listOf(wildHappening))
    }
  }

}

object BarAdderStave : EventAdder {
  override fun addEvent(
    happening: Happening,
    scoreLevel: ScoreLevel,
    scoreQuery: ScoreQuery?
  ): EventChangeReturn? {
    return BarAdderLevel.addEvent(happening, scoreLevel, scoreQuery)?.let { ecr ->
      scoreQuery?.getTimeSignature(happening.originalAddress)?.let { ts ->

        happening.event.getParam<Int>(EventParam.NUMBER)?.let { numBars ->
          val newSls = scoreLevel.subLevels.toMutableList()
          repeat(numBars) {
            newSls.add(happening.eventAddress.barNum - 1, Bar(ts))
          }
          EventChangeReturn(ecr.newMap, newSls)
        }
      }
    }
  }

  override fun deleteEvent(
    happening: Happening,
    scoreLevel: ScoreLevel,
    scoreQuery: ScoreQuery?
  ): EventChangeReturn? {
    return BarAdderLevel.deleteEvent(happening, scoreLevel, scoreQuery)?.let { ecr ->

      happening.event.getParam<Int>(EventParam.NUMBER)?.let { numBars ->
        val realNum = min(numBars, (scoreQuery?.numBars ?: 1) - happening.eventAddress.barNum + 1)
        val newSls = scoreLevel.subLevels.toMutableList()
        repeat(realNum) {
          newSls.removeAt(happening.eventAddress.barNum - 1)
        }
        EventChangeReturn(ecr.newMap, newSls)
      }
    }
  }
}
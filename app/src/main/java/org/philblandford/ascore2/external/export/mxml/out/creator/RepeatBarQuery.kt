package com.philblandford.ascore.external.export.mxml.out.creator

import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.map.EventList
import com.philblandford.kscore.engine.map.eventHashOf
import com.philblandford.kscore.engine.types.*

internal data class RepeatBarDesc(val start: Boolean, val num: Int)

internal interface RepeatBarQuery {
  fun getRepeatBars(eventAddress: EventAddress): Iterable<RepeatBarDesc>
}

private class RepeatBarQueryImpl(val map: Map<EventAddress, Iterable<RepeatBarDesc>>) :
  RepeatBarQuery {
  override fun getRepeatBars(eventAddress: EventAddress): Iterable<RepeatBarDesc> {
    return map[eventAddress] ?: listOf()
  }
}

internal fun repeatBarQuery(scoreQuery: ScoreQuery): RepeatBarQuery {
  val events = scoreQuery.getEvents(EventType.REPEAT_BAR) ?: eventHashOf()

  val grouped = events.toList().groupBy { it.first.eventAddress.staveId }

  val map = grouped.flatMap { (staveId, eventList) ->
    val barMap = getMapForStave(eventList)
    barMap.map { (bar, descs) ->
      eas(bar, dZero(), staveId) to descs
    }
  }.toMap()


  return RepeatBarQueryImpl(map)
}

private fun getMapForStave(eventList: EventList): Map<BarNum, Iterable<RepeatBarDesc>> {

  var currentStart: Pair<BarNum,Int>? = null
  var currentEnd: Int? = null

  val descs = mutableMapOf<Int, Iterable<RepeatBarDesc>>()

  fun addDesc(pos: Int, desc: RepeatBarDesc) {
    val list = descs[pos] ?: listOf()
    descs.put(pos, list.plus(desc))
  }

  eventList.forEach { (k, event) ->

    val thisBars = event.getInt(EventParam.NUMBER, 1)
    currentStart?.let { (cur,nBars) ->
      currentEnd?.let { end ->
        if (k.eventAddress.barNum > end + 1) {
          addDesc(cur, RepeatBarDesc(true, nBars))
          addDesc(currentEnd ?: cur, RepeatBarDesc(false, nBars))
          currentStart = k.eventAddress.barNum to thisBars
          currentEnd = k.eventAddress.barNum + nBars - 1
        } else {
          currentEnd = k.eventAddress.barNum + nBars - 1
        }
      }
    } ?: run {
      currentStart = k.eventAddress.barNum to thisBars
      currentEnd = k.eventAddress.barNum + thisBars - 1

    }
  }

  currentStart?.let { (cur,nBars) ->
    addDesc(cur, RepeatBarDesc(true, nBars))
    addDesc(currentEnd ?: (cur + nBars-1), RepeatBarDesc(false, nBars))
  }
  return descs
}
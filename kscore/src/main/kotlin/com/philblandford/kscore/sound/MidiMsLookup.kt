package com.philblandford.kscore.sound

import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.tempo.Tempo
import com.philblandford.kscore.engine.tempo.tempo
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.log.ksLogv


fun midiMsLookup(eventLookup: EventLookup): MsLookup {
  ksLogv("Creating ms lookup")

  val complete = mutableMapOf<Int, Offset>()
  val eventList = eventLookup.toList()
  var currentTempo =
    eventList.firstOrNull()?.let { getTempo(it.second) } ?: Tempo(crotchet(), 120)
  var currentMs = 0
  var lastLocation = dZero()

  eventList.forEach { (offset, events) ->
    val extra = durationToMs(offset.subtractC(lastLocation), currentTempo)
    complete[currentMs + extra] = offset
    currentMs += extra

    getTempo(events)?.let { currentTempo = it }

    events[EventType.DURATION]?.forEach {
      val durationMs = durationToMs(it.event.realDuration(), currentTempo)
      val endOffset = offset.addC(it.event.realDuration())
      complete.put(currentMs + durationMs, endOffset)
    }
    lastLocation = offset
  }

  return complete.toSortedMap()
}

private fun getTempo(events: EMSimple): Tempo? {
  return events[EventType.TEMPO]?.firstOrNull()?.let { tempo(it.event) }
}

internal fun durationToMs(duration: Duration, tempo: Tempo): Int {
  return if (duration == dZero()) {
    0
  } else {
    val msPerUnit = Duration(60000) / tempo.bpm
    val ratio = duration / tempo.duration
    (ratio * msPerUnit).toInt()
  }
}

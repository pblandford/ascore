package com.philblandford.kscore.engine.tempo

import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.paramMapOf

data class Tempo(val duration: Duration, val bpm:Int) {

  fun toEvent():Event {
    return Event(EventType.TEMPO, paramMapOf(EventParam.DURATION to duration, EventParam.BPM to bpm))
  }
}

fun tempo(event:Event):Tempo? {
  return event.getParam<Duration>(EventParam.DURATION)?.let { duration ->
    event.getParam<Int>(EventParam.BPM)?.let { bpm ->
      Tempo(duration, bpm)
    }
  }
}
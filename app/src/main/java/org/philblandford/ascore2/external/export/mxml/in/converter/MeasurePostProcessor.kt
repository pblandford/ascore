package com.philblandford.ascore.external.export.mxml.`in`.converter

import com.philblandford.kscore.engine.core.score.Bar
import com.philblandford.kscore.engine.core.score.VoiceMap
import com.philblandford.kscore.engine.core.score.tuplet
import com.philblandford.kscore.engine.core.score.voiceMap
import com.philblandford.kscore.engine.dsl.tupletMarker
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.duration.duration
import com.philblandford.kscore.engine.map.EMK
import com.philblandford.kscore.engine.map.emptyEventMap
import com.philblandford.kscore.engine.map.eventHashOf
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.types.*

internal fun createBars(measureState: MeasureState): Map<Int, Bar> {
  val voiceMaps = createVoiceMaps(measureState.events, measureState.attributes)

  val byStaff = voiceMaps.toList().groupBy { it.first.staff }
  val bars = byStaff.map { (staff, vmList) ->
    val byVoice = vmList.toList().groupBy { it.first.voice }
    val vmsForBar = byVoice.flatMap { it.value.map { it.second } }
    staff to Bar(measureState.attributes.timeSignature, vmsForBar)
  }.toMap()
  return createBarVoiceMap(measureState.barEvents, bars)
}


internal fun createVoiceMaps(
  events: EventMap,
  attributes: OngoingAttributes
): Map<VMKey, VoiceMap> {
  val eventMaps = mutableMapOf<VMKey, EventMap>()

  val ts = TimeSignature(attributes.timeSignature.numerator, attributes.timeSignature.denominator)
  events.getAllEvents().forEach { (k, v) ->
    val key = VMKey(k.eventAddress.staveId.sub, k.eventAddress.voice)
    val map = eventMaps[key] ?: emptyEventMap()
    eventMaps[key] = map.putEvent(k.eventAddress.copy(staveId = sZero(), voice = 0), v)
  }
  return eventMaps.map { (k, v) -> k to createVoiceMap(v, ts) }.toMap()
}

private fun createVoiceMap(events: EventMap, ts: TimeSignature): VoiceMap {
  val newEvents = events.getEvent(EventType.DURATION, eZero())?.let {
    if (it.subType == DurationType.REST && it.duration() == ts.duration) {
      events.deleteAll(EventType.DURATION)
    } else events
  } ?: events
  var vm = voiceMap(ts, newEvents, listOf(), createBeams = false)
  vm = setActualTimeSignature(vm, ts)
  vm = createTuplets(vm)

  return vm
}

private fun createTuplets(voiceMap: VoiceMap): VoiceMap {
  val tuplets = voiceMap.eventMap.getEvents(EventType.TUPLET)?.mapNotNull {
    tuplet(it.value, it.key.eventAddress.offset)
  } ?: listOf()

  return if (tuplets.isNotEmpty()) {

    var durationEvents = voiceMap.eventMap.getEvents(EventType.DURATION) ?: eventHashOf()
    val newTuplets = tuplets.map { tuplet ->
      val dEvents = durationEvents.filter {
        it.key.eventAddress.offset >= tuplet.offset &&
            tuplet.offset.add(tuplet.realDuration) > it.key.eventAddress.offset
      }
      durationEvents = durationEvents.minus(dEvents.keys)
      val converted = dEvents.map { (k, v) ->
        val offset = tuplet.realToWritten(k.eventAddress.offset.subtract(tuplet.offset))
        EMK(EventType.DURATION, ez(0, offset)) to v
      }.toMap()
      tuplet(tuplet, tuplet.eventMap.replaceEvents(EventType.DURATION, converted))
    }
    durationEvents = tuplets.fold(durationEvents) { de, tuplet ->
      de.plus(
        EMK(
          EventType.DURATION,
          ez(0, tuplet.offset)
        ) to tupletMarker(tuplet.realDuration)
      )
    }
    voiceMap(
      voiceMap.timeSignature,
      voiceMap.eventMap.replaceEvents(
        EventType.DURATION,
        durationEvents
      ).deleteAll(EventType.TUPLET),
      newTuplets
    )
  } else {
    voiceMap
  }
}

private fun createBarVoiceMap(events: EventMap, bars: Map<Int, Bar>): Map<Int, Bar> {
  val newMap = mutableMapOf<Int, EventMap>()

  events.getAllEvents().forEach { (key, event) ->
    val staff = key.eventAddress.staveId.sub
    var em = newMap[staff] ?: emptyEventMap()
    em = em.putEvent(key.eventAddress.staveless(), event)
    newMap.put(staff, em)
  }
  return bars.map { (k, bar) ->
    k to Bar(bar.timeSignature, bar.voiceMaps, newMap[k] ?: emptyEventMap())
  }.toMap()
}

private fun setActualTimeSignature(voiceMap: VoiceMap, timeSignature: TimeSignature): VoiceMap {
  val actualDuration = voiceMap.actualDuration()
  return if (actualDuration != dZero() && actualDuration != timeSignature.duration) {
    voiceMap(
      TimeSignature(actualDuration.numerator, actualDuration.denominator, hidden = true),
      voiceMap.eventMap, voiceMap.tuplets, createBeams = false
    )
  } else voiceMap
}

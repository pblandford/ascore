package com.philblandford.kscore.engine.core.score

import com.philblandford.kscore.engine.dsl.rest
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.duration.addC
import com.philblandford.kscore.engine.duration.realDuration
import com.philblandford.kscore.engine.map.eventHashOf
import com.philblandford.kscore.engine.types.DurationType
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.eZero
import com.philblandford.kscore.engine.util.replace

typealias VoiceNumberMap = Map<Duration, Int>

fun VoiceNumberMap.voicesAt(offset: Duration): Int {
  return get(offset) ?: 1
}

fun voiceNumberMap(voiceMaps: Iterable<VoiceMap>): VoiceNumberMap {

  /* Replace any empty maps with a whole bar rest */
  val treated = fillEmptyMapsWithRest(voiceMaps)

  /* Initialise the map of offsets to number of voices with zero values */
  val map = treated.flatMap { vm -> vm.getVoiceEvents().map { it.key to 0 } }.toMap().toMutableMap()

  /* At each offset, determine the number of notes in each voice still sounding */
  treated.forEach { vm ->
    vm.getVoiceEvents().filterNot { it.value.subType == DurationType.EMPTY }
      .forEach { (key, event) ->
        /* For this event, how previous events overlap it? */
        map.filter { key <= it.key && it.key < key.addC(event.realDuration()) }
          .forEach { (offset, num) ->
            map[offset] = num + 1
          }
      }
  }
  return map
}

private fun fillEmptyMapsWithRest(voiceMaps:Iterable<VoiceMap>):Iterable<VoiceMap> {
  return voiceMaps.firstOrNull()?.let { voiceMap ->
    val events = voiceMap.getEvents(EventType.DURATION) ?: eventHashOf()
    if (events.isEmpty()) {
      val new =
        VoiceMap(
          voiceMap.timeSignature,
          voiceMap.eventMap.putEvent(eZero(), rest(voiceMap.timeSignature.duration))
        )
      val mutable = voiceMaps.toMutableList()
      mutable.replace(0, new)
    } else {
      voiceMaps
    }
  } ?: voiceMaps
}
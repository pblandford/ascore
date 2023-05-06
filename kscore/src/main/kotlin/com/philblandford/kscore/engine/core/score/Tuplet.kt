package com.philblandford.kscore.engine.core.score

import com.philblandford.kscore.engine.beam.Beam
import com.philblandford.kscore.engine.beam.BeamMap
import com.philblandford.kscore.engine.beam.BeamMember
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.cZero
import com.philblandford.kscore.engine.dsl.rest
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.map.*
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.types.*

/*
 * A tuplet is represented as a sublevel of a VoiceMap. It is also a subclass of a VoiceMap,
 * meaning it adds and deletes duration events the same way and has its own local timesignature.
 * Essentially it's a Voicemap within a Voicemap
 */

data class Tuplet(
  val offset: Offset,
  override val timeSignature: TimeSignature,
  val childDivisor: Int,
  val realDuration: Duration,
  val hidden: Boolean,
  val events: EventMap = emptyEventMap(),
  val hardStart:Coord = cZero(),
  override val beamMap: BeamMap = mapOf()
) :
  VoiceMap(timeSignature, events.putEvent(eZero(), timeSignature.toEvent()), beamMap = beamMap) {
  val duration = timeSignature.duration

  private val ratioToReal: Duration = realDuration / timeSignature.duration
  val members = getVoiceEvents().keys.sorted()
  val lastMember = members.maxOrNull() ?: dZero()

  override fun replaceSelf(eventMap: EventMap, newSubLevels: Iterable<ScoreLevel>?): ScoreLevel {
    return if (eventMap.getEvents(EventType.DURATION)?.isEmpty() != false) {
      /* if we have lost all our events, recreate our rests */
      tuplet(offset, timeSignature.numerator, timeSignature.denominator, hidden).copy(hardStart = hardStart)
    } else {
      tuplet(this, eventMap)
    }
  }

  override fun getSpecialEvent(eventType: EventType, eventAddress: EventAddress): Event? {
    return when (eventType) {
      EventType.DURATION -> {
        eventMap.getEvent(eventType, eventAddress)?.let { transformDurationEvent(it) }
      }
      else -> null
    }
  }

  override fun getSpecialEvents(eventType: EventType): EventHash? {
    return when (eventType) {
      EventType.DURATION, EventType.TIE -> {
        return eventMap.getEvents(eventType)?.map { (k, v) ->
          k.copy(eventAddress = k.eventAddress.copy(offset = getRealOffset(k.eventAddress.offset))) to
              transformDurationEvent(v)
        }?.toMap()
      }
      else -> null
    }
  }

  override fun stripAddress(eventAddress: EventAddress, eventType: EventType): EventAddress {
    return eventAddress.copy(offset = getLocalOffset(eventAddress.offset))
  }

  fun toEvent(): Event {
    return Event(
      EventType.TUPLET, paramMapOf(
        EventParam.NUMERATOR to timeSignature.numerator,
        EventParam.DENOMINATOR to timeSignature.denominator,
        EventParam.DURATION to realDuration,
        EventParam.DIVISOR to childDivisor,
        EventParam.HIDDEN to hidden,
        EventParam.HARD_START to hardStart,
        EventParam.MEMBERS to members,
      )
    )
  }

  fun writtenToReal(original: Duration): Duration {
    return original.multiply(ratioToReal)
  }

  fun realToWritten(real: Duration): Duration {
    return real.divide(ratioToReal)
  }

//  override fun deleteRange(start: EventAddress, end: EventAddress): ScoreLevel {
//    val startOffset = max(getLocalOffset(start.offset), dZero())
//    val endOffset = getLocalOffset(end.offset)
//    return super.deleteRange(ez(0, startOffset), ez(0, endOffset))
//  }

  override fun addEmpties(): Tuplet {
    val map = super.addEmpties().eventMap
    return Tuplet(offset, timeSignature, childDivisor, realDuration, hidden, map)
  }

  fun replaceVoiceEvent(absoluteAddress: EventAddress, event: Event): Tuplet {
    val relativeOffset = realToWritten(absoluteAddress.offset.subtract(offset))
    val newEvents = eventMap.putEvent(absoluteAddress.copy(offset = relativeOffset), event)
    return tuplet(
      offset,
      timeSignature.numerator,
      timeSignature.denominator,
      childDivisor,
      realDuration,
      hidden,
      newEvents
    )
  }

  private fun getLocalOffset(addressOffset: Duration): Duration {
    if (addressOffset == dWild()) return dWild()
    val diff = addressOffset.subtract(offset)
    return diff.divide(ratioToReal)
  }

  private fun getRealOffset(localOffset: Duration): Duration {
    val localReal = localOffset.multiply(ratioToReal)
    return offset.add(localReal)
  }

  private fun transformDurationEvent(event: Event): Event {
    val realDuration = event.duration().multiply(ratioToReal)
    return event.addParam(EventParam.REAL_DURATION, realDuration)
  }

  fun transformDurationEvents(
    barAddressStart: EventAddress, barAddressEnd: EventAddress?,
    func: (Event) -> (Event)
  ): Tuplet {
    val startLocal = barAddressStart.offset.subtract(offset)
    val endLocal =
      barAddressEnd?.let { if (it.offset.isWild()) null else it.offset.subtract(offset) }
    val events = eventMap.getEvents(EventType.DURATION)?.map { (key, event) ->
      val barOffset = writtenToReal(key.eventAddress.offset).add(offset)
      val ev = if (barOffset >= startLocal &&
        (endLocal?.isWild() == true || endLocal?.let { it > barOffset } != false)
      ) {
        func(event)
      } else {
        event
      }
      key to ev
    }?.toMap()
    val newEm = events?.let { eventMap.replaceEvents(EventType.DURATION, events) } ?: eventMap
    return tuplet(this, newEm)
  }
}

private fun getChildDivisor(numerator: Int, compound: Boolean): Int {
  return when {
    numerator == 2 -> 3
    numerator == 3 -> 2
    numerator == 4 -> if (compound) 6 else 3
    numerator in 5..7 -> if (compound) 3 else 4
    numerator == 8 -> 6
    numerator == 10 -> if (compound) 6 else 8
    else -> 8
  }
}

fun tuplet(event: Event, offset: Duration = dZero()): Tuplet? {
  return event.getParam<Int>(EventParam.NUMERATOR)?.let { numerator ->
    event.getParam<Int>(EventParam.DENOMINATOR)?.let { denominator ->
      event.getParam<Int>(EventParam.DIVISOR)?.let { divisor ->
        event.getParam<Duration>(EventParam.DURATION)?.let { duration ->
          val hidden = event.isTrue(EventParam.HIDDEN)
          val hardStart = event.getParam<Coord>(EventParam.HARD_START) ?: cZero()
          Tuplet(
            offset, TimeSignature(numerator, denominator), divisor, duration,
            hidden, emptyEventMap(), hardStart
          )
        }
      }
    }
  }
}

fun tuplet(
  offset: Duration, numerator: Int, denominator: Int, childDivisor: Int,
  duration: Duration, hidden: Boolean = false,
  eventMap: EventMap = emptyEventMap()
): Tuplet {
  return tuplet(
    Tuplet(
      offset, TimeSignature(numerator, denominator), childDivisor,
      duration, hidden, eventMap
    ), eventMap
  )
}

fun tuplet(
  offset: Duration, numerator: Int, denominator: Int, hidden: Boolean = false,
  hardStart: Coord = Coord(),
  eventMap: EventMap = emptyEventMap()
): Tuplet {
  val childDivisor = getChildDivisor(numerator, false)
  val ratio = Duration(numerator, childDivisor)
  val ts = TimeSignature(numerator, denominator)
  val realDuration = ts.duration.divide(ratio)
  return tuplet(offset, numerator, realDuration, hidden, hardStart, eventMap)
}

fun tuplet(
  offset: Duration, numerator: Int, duration: Duration, hidden: Boolean = false,
  hardStart: Coord = Coord(),
  eventMap: EventMap = emptyEventMap()
): Tuplet {
  val childDivisor = getChildDivisor(numerator, duration.numerator % 3 == 0)
  val tsFraction = duration.divide(childDivisor).undot().multiply(numerator)
  val mult = numerator / tsFraction.numerator
  val timeSignature = TimeSignature(tsFraction.numerator * mult, tsFraction.denominator * mult)
  val tuplet = Tuplet(
    offset, timeSignature, childDivisor, duration, hidden,
    emptyEventMap().putEvent(eZero(), timeSignature.toEvent())
  )
  val newMap = createRests(eventMap, timeSignature)
  return tuplet(tuplet, newMap)
}

fun tuplet(tuplet: Tuplet, eventMap: EventMap): Tuplet {
  var newMap = transformDurations(eventMap, tuplet)
  val beamMap = createTupletBeams(newMap.getEvents(EventType.DURATION) ?: eventHashOf(), tuplet)
  newMap = markBeams(newMap, beamMap)
  newMap = addTies(newMap)
  return Tuplet(
    tuplet.offset, tuplet.timeSignature, tuplet.childDivisor, tuplet.realDuration, tuplet.hidden,
    newMap, tuplet.hardStart, beamMap
  )
}


private fun createRests(eventMap: EventMap, timeSignature: TimeSignature): EventMap {
  val beatDuration = Duration(1, timeSignature.denominator)

  return if ((eventMap.getEvents(EventType.DURATION) ?: eventHashOf()).isEmpty()) {
    val rests = (0 until timeSignature.numerator).map {
      val os = beatDuration.multiply(it)
      EventMapKey(EventType.DURATION, ez(0, os)) to rest(beatDuration)
    }
    eventMap.replaceEvents(EventType.DURATION, rests.toMap())
  } else eventMap
}

private fun transformDurations(eventMap: EventMap, tuplet: Tuplet): EventMap {
  return eventMap.getEvents(EventType.DURATION)?.let { durationEvents ->
    val fixed = durationEvents.map { (k, v) ->
      val realDuration = tuplet.writtenToReal(v.duration())
      val event =
        chord(v)?.let { it.transformNotes { it.copy(realDuration = realDuration) } }?.toEvent() ?: v
      k to event.addParam(EventParam.REAL_DURATION, realDuration)
    }.toMap()
    eventMap.replaceEvents(EventType.DURATION, fixed)
  } ?: eventMap
}

private fun markBeams(eventMap: EventMap, beamMap: BeamMap): EventMap {
  val beamedEvents = eventMap.getEvents(EventType.DURATION)?.let {
    markBeamGroupMembers(beamMap, eventHashOf(), it)
  } ?: eventHashOf()
  return eventMap.replaceEvents(EventType.DURATION, beamedEvents)
}

private fun createTupletBeams(durationEvents: EventHash, tuplet: Tuplet): BeamMap {
  if (durationEvents.count { it.value.subType == DurationType.CHORD } <= 1) {
    return mapOf()
  }
  val groups = getSubCrotchetGroups(durationEvents)
  return groups.map { events ->
    val members = events.map { BeamMember(it.second.duration(), it.second.realDuration()) }
    val up = getUp(durationEvents)
    events.first().first.eventAddress to Beam(members, tuplet.duration, up)
  }.toMap()
}

private fun getSubCrotchetGroups(durationEvents: EventHash): Iterable<EventList> {
  val groups = mutableListOf<EventList>()

  var list = durationEvents.toList()
  while (list.isNotEmpty()) {
    val group =
      list.takeWhile { it.second.duration() < crotchet() && it.second.subType == DurationType.CHORD }
    if (group.size > 1) {
      groups.add(group)
    }
    list = list.drop(group.size + 1)
  }
  return groups
}

private fun getUp(members: EventHash): Boolean {
  val noRest = members.filterNot { it.value.subType == DurationType.REST }.toList()
  val grouping = noRest.groupingBy { it.second.isTrue(EventParam.IS_UPSTEM) }.eachCount()
  return (grouping[true] ?: 0) >= (grouping[false] ?: 0)
}
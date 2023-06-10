package com.philblandford.kscore.engine.beam

import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.map.EventHash
import com.philblandford.kscore.engine.map.EventList
import com.philblandford.kscore.engine.map.EventMapKey
import com.philblandford.kscore.engine.map.eventHashOf
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.log.ksLoge
import com.philblandford.kscore.log.ksLogv
import com.philblandford.kscore.util.sumOf

data class BeamMember(val offset:Duration, val duration: Duration, val realDuration: Duration)
data class SimpleDuration(
  val duration: Duration, val realDuration: Duration,
  val durationType: DurationType, val upstem: Boolean
)

data class Beam(
  val members: List<BeamMember>, val duration: Duration = members.sumOf { it.realDuration }, val up: Boolean = true,
  val endMarker: Boolean = false
) {
  fun toEvent(): Event {
    return Event(
      EventType.BEAM, paramMapOf(
        EventParam.DURATION to duration,
        EventParam.MEMBERS to members,
        EventParam.IS_UPSTEM to up,
        EventParam.END to endMarker
      )
    )
  }

  fun getBeamString(): String {
    return members.map {
        if (it.duration.numerator == 1) {
          it.duration.denominator
        } else {
          "${it.duration.numerator}/${it.duration.denominator}"
        }
      }.joinToString(":")
  }
}

fun beam(event: Event): Beam {
  val endMarker = when (val end = event.params[EventParam.END]) {
    is Boolean -> end
    else -> false
  }

  return Beam(
    event.params[EventParam.MEMBERS] as List<BeamMember>,
    event.params[EventParam.DURATION] as Duration,
    event.params[EventParam.IS_UPSTEM] as Boolean,
    endMarker
  )
}

typealias BeamMap = Map<EventAddress, Beam>

fun beamMapOf() = mapOf<EventAddress, Beam>()
private typealias BeamMapM = MutableMap<EventAddress, Beam>


private data class CacheKey(
  val simple: Map<Offset, SimpleDuration>, val user: EventHash,
  val numerator: Int, val denominator: Int
)

private val cache = mutableMapOf<CacheKey, BeamMap>()

fun createBeams(
  notes: EventHash,
  userBeams: EventHash = eventHashOf(),
  timeSignature: TimeSignature = TimeSignature(4, 4)
): BeamMap {

  val graceNormal = notes.toList().partition { it.first.eventAddress.isGrace }

  val grace = createGrace(graceNormal.first)
  val normal = doCreateBeams(graceNormal.second, userBeams, timeSignature)
  return grace.plus(normal)
}

private fun createGrace(eventList: EventList): BeamMap {
  val grouped = eventList.groupBy { it.first.eventAddress.offset }

  return grouped.toList().filter { it.second.size > 1 }.fold(beamMapOf()) { bm, (_, eventList) ->

    val sorted = eventList.sortedBy { it.first.eventAddress.graceOffset }
    val groups = getGraceGroups(sorted)

    groups.fold(bm) { bm2, group ->
      val members = group.map {
        BeamMember(it.first.eventAddress.graceOffset ?: dZero(), it.second.duration(), it.second.realDuration())
      }
      val up = getUp(sorted.toSimple().toList())
      val duration =
        sorted.last().first.eventAddress.graceOffset?.addC(sorted.last().second.realDuration())
          ?: dZero()
      val beam = Beam(members, duration, up)

      bm2.plus(group.toList().first().first.eventAddress to beam)
    }
  }
}

private fun getGraceGroups(members: List<Pair<EventMapKey, Event>>): List<List<Pair<EventMapKey, Event>>> {
  val res = mutableListOf<List<Pair<EventMapKey, Event>>>()
  var remaining = members

  while (remaining.isNotEmpty()) {
    val group =
      remaining.takeWhile { it.second.duration() < CROTCHET && it.second.subType == DurationType.CHORD }
    if (group.size > 1) {
      res.add(group)
    }
    remaining = remaining.drop(group.size)
      .dropWhile { it.second.duration() >= CROTCHET || it.second.subType != DurationType.CHORD }
  }
  return res
}

private fun doCreateBeams(
  notes: EventList,
  userBeams: EventHash,
  timeSignature: TimeSignature
): BeamMap {

  val simpleNotes = notes.toSimple()
  val key = CacheKey(simpleNotes, userBeams, timeSignature.numerator, timeSignature.denominator)
  return cache[key] ?: run {
    val mutable = mutableMapOf<EventAddress, Beam>()
    val userMembers = markUserBeamMembers(simpleNotes, userBeams)
    createBeamsForBar(simpleNotes, userMembers, timeSignature, mutable)
    cache[key] = mutable
    return mutable.toMap()
  }
}

private fun EventList.toSimple(): Map<Offset, SimpleDuration> {
  return mapNotNull {
        (it.second.subType as? DurationType)?.let { type ->
          it.first.eventAddress.offset to
              SimpleDuration(
            it.second.duration(), it.second.realDuration(),
            type, it.second.isTrue(EventParam.IS_UPSTEM)
          )
        }
  }.sortedBy { it.first }.toMap()
}

private fun createBeamsForBar(
  notes: Map<Offset, SimpleDuration>, userMembers: Set<Duration>,
  timeSignature: TimeSignature, beamMap: BeamMapM
) {
  createBeamsForSection(notes, userMembers, timeSignature, dZero(), beamMap)
}

private fun createBeamsForSection(
  notes: Map<Offset, SimpleDuration>, userMembers: Set<Duration>,
  timeSignature: TimeSignature, offset: Duration, beamMap: BeamMapM
) {

  if (stopNow(notes, timeSignature)) {
    return
  }

  val remaining = notes.toList().toMutableList()

  getBeam(notes, userMembers, timeSignature)?.let { (offset, beam) ->
    beamMap[ez(0, offset)] = beam
    remaining.removeAll(notes.toList())
  }
  val nextTimeSignatures = divideTimeSignature(timeSignature, offset)

  if (nextTimeSignatures.any { !it.first.isValid() }) {
    ksLogv("$notes $timeSignature $nextTimeSignatures")
    ksLoge("Bad division $nextTimeSignatures")
    return
  }

  nextTimeSignatures.forEach { (ts, offset) ->
    val noteGroup = remaining.filter {
      it.first >= offset &&
          it.first < offset.add(ts.duration)
    }.toMap()
    createBeamsForSection(noteGroup, userMembers, ts, offset, beamMap)
  }
}

private fun getBeam(
  notes: Map<Offset, SimpleDuration>,
  userMembers: Set<Duration>,
  timeSignature: TimeSignature
): Pair<Offset, Beam>? {

  val filtered =
    notes.filterNot {
      it.value.duration >= crotchet() || it.value.durationType != DurationType.CHORD
          || userMembers.contains(it.key)
    }

  val contiguous = !filtered.toList().sortedBy { it.first }.windowed(2).any {
    it.first().first.add(it.first().second.duration) != it.last().first
  }


  if (contiguous && filtered.size > 1) {

    val smallestNote = getSmallestNote(filtered)
    smallestNote?.let { sn ->

      val divisors = if (timeSignature.numerator == 3) listOf(6, 3) else listOf(4, 2)

      divisors.forEach { divisor ->
        if (sn == timeSignature.duration.divide(divisor)) {
          val up = getUp(filtered.toList())
          val address = filtered.toList().minByOrNull { it.first }!!.first
          val startOffset = filtered.toList().first().first
          return address to Beam(
            filtered.map { BeamMember(it.key - startOffset, it.value.duration, it.value.realDuration) },
            timeSignature.duration, up
          )
        }
      }
    }
  }
  return null
}

private fun getSmallestNote(filtered: Map<Offset, SimpleDuration>): Duration? {
  val smallestNote = filtered.minByOrNull { it.value.duration }?.value?.duration
  smallestNote?.let {
    if (filtered.count { it.value.duration.numDots() > 0 } > 1) {
      return smallestNote.multiply(2)
    } else {
      filtered.toList().find { it.second.duration.numDots() > 0 }?.let { dotted ->
        if (filtered.any { it.value.duration == dotted.second.duration / 6 }) {
          return dotted.second.duration.undot()
        }
      }
    }
  }
  return smallestNote
}

private fun stopNow(notes: Map<Offset, SimpleDuration>, timeSignature: TimeSignature): Boolean {
  if (timeSignature.numerator % 3 == 0) {
    return notes.filter { it.value.duration.denominator >= timeSignature.duration.denominator }
      .isEmpty()
  }
  return notes.filter { it.value.duration <= timeSignature.duration.divide(2) }.isEmpty()
}

fun getUp(members: List<Pair<Offset, SimpleDuration>>): Boolean {
  val noRest = members.filterNot { it.second.durationType == DurationType.REST }
  val grouping = noRest.groupingBy { it.second.upstem }.eachCount()
  return (grouping[true] ?: 0) >= (grouping[false] ?: 0)
}


private fun divideTimeSignature(
  timeSignature: TimeSignature,
  offset: Duration
): Iterable<Pair<TimeSignature, Duration>> {
  irregularDivisions[timeSignature.numerator]?.let {
    var o = offset
    return it.map { num ->
      val thisOffset = o
      val ts = TimeSignature(num, timeSignature.denominator)
      o = o.add(ts.duration)
      ts to thisOffset
    }
  }
  val divisor = if (timeSignature.numerator % 3 == 0) {
    if (timeSignature.numerator > 3) timeSignature.numerator / 3 else 3
  } else {
    2
  }

  return (0 until divisor).map {
    val newTs = timeSignature.divide(divisor)
    newTs to offset.add(newTs.duration.multiply(it))
  }
}

private val irregularDivisions = mapOf(
  5 to listOf(3, 2),
  7 to listOf(4, 3)
)

private fun markUserBeamMembers(
  notes: Map<Offset, SimpleDuration>,
  beams: EventHash
): Set<Duration> {

  fun isMember(offset: Duration): Boolean {
    return beams.any {
      it.key.eventAddress.offset <= offset &&
          it.key.eventAddress.offset.add(it.value.duration()) >= offset
    }
  }

  return notes.map { it.key to isMember(it.key) }.filter { it.second }
    .map { it.first }.toSet()
}

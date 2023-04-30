package com.philblandford.kscore.engine.eventadder.duration

import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.eventadder.*
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.time.getRegularDivisions
import com.philblandford.kscore.engine.types.DurationType
import java.util.*

typealias DMResult = AnyResult<DurationMap>

data class DEvent(val duration: Duration, val type: DurationType = DurationType.CHORD)

data class DurationMap(
  val timeSignature: TimeSignature,
  val map: SortedMap<Offset, DEvent> = sortedMapOf(),
  val offset: Offset = dZero()
) {
  override fun toString(): String {
    return eventString()
  }
}

fun DurationMap.add(dEvent: DEvent, offset: Offset, consolidate: Boolean = false): DMResult {
  return sanityCheck(dEvent, offset)
    .then {
      var newDm = removeOverlapping(dEvent.duration, offset)

      newDm = newDm.copy(map = newDm.map.plus(offset to dEvent).toSortedMap())
      newDm = if (consolidate) {
        newDm.removeRests()
      } else {
        newDm.removeNeighbours(offset)
      }
      Right(newDm.tidy())
    }
}

fun DurationMap.delete(offset: Offset, consolidate: Boolean = false): DMResult {
  var newDm = if (consolidate) {
    removeRests()
  } else {
    removeNeighbours(offset)
  }
  newDm = newDm.copy(map = newDm.map.minus(offset).toSortedMap())
  return Right(newDm.tidy())
}

private fun DurationMap.sanityCheck(dEvent: DEvent, offset: Offset): DMResult {
  return if (dEvent.duration <= dZero()) {
    Left(Error("Duration ${dEvent.duration}"))
  } else if (offset + dEvent.duration > timeSignature.duration) {
    Left(Error("$offset plus ${dEvent.duration} greater than bar length"))
  } else if (offset < dZero()) {
    Left(Error("Offset $offset < 0"))
  } else {
    Right(this)
  }
}

private fun DurationMap.removeNeighbours(offset: Offset): DurationMap {
  return map[offset]?.let { victim ->
    val neighbours = restNeighbours(victim.duration, offset)
    copy(map = map.filterNot { it.value.type == DurationType.REST && neighbours.contains(it.key) }
      .toMap().toSortedMap())
  } ?: this
}

private fun DurationMap.removeRests(): DurationMap {
  return copy(map = map.filterNot { it.value.type == DurationType.REST }.toMap().toSortedMap())
}

fun DurationMap.reset(): DurationMap {
  return removeRests().tidy()
}

fun DurationMap.tidy(): DurationMap {
  return removeOutsizeRests().addRests()
}

private fun DurationMap.removeOutsizeRests(): DurationMap {
  val filtered = map.toList().windowed(2).fold(map) { m, (one, two) ->
    if (one.first + one.second.duration > two.first) {
      m.minus(one.first).toSortedMap()
    } else {
      m
    }
  }.toSortedMap()
  return copy(map = filtered)
}

private fun DurationMap.restNeighbours(duration: Duration, offset: Offset): List<Offset> {
  if (offset.denominator == duration.denominator) {
    return listOf(offset - duration)
  }
  return listOf(offset + duration)
}

private fun DurationMap.addSimple(dEvent: DEvent, offset: Offset): DurationMap {
  val newMap = map.plus(offset to dEvent).toSortedMap()
  return copy(map = newMap)
}

fun DurationMap.addRests(): DurationMap {

  if (map.isEmpty()) {
    return if (timeSignature.hidden) {
      val events = timeSignature.getRegularDivisions().map {(offset, duration) ->
        offset to DEvent(duration, DurationType.REST)
      }
       copy(map = events.toMap().toSortedMap())
    } else this
  }

  return split()?.let { maps ->
    val mapList = maps.map { m ->
      if (m.map.isEmpty())
        m.addSimple(DEvent(m.timeSignature.duration, DurationType.REST), dZero())
      else m.addRests()
    }
    mapList.merge(timeSignature)
  } ?: this
}

/* DM offsets are absolute, event offsets are relative to them */
private fun DurationMap.split(): List<DurationMap>? {
  val first = map.toList().first()
  if (first.first == dZero() && first.first + first.second.duration >= timeSignature.duration) {
    return null
  }
  var splitDmList = divideMap()
  splitDmList = splitDmList.putEmptyMarkers()

  return splitDmList
}

private fun DurationMap.divideMap(): List<DurationMap> {
  val dividedTimes = timeSignature.split()
  var splitDmList =
    dividedTimes.map { (offset, ts) -> DurationMap(ts, offset = offset + this.offset) }
  val terminator = splitDmList.last().let { last ->
    val offset = last.offset + last.timeSignature.duration
    last.copy(offset = offset)
  }
  val terminated = splitDmList.plus(terminator)
  splitDmList = terminated.windowed(2).map { (one, two) ->
    var events =
      map.filter { it.key >= (one.offset - this.offset) && it.key < (two.offset - this.offset) }
    events = events.map { it.key - (one.offset - this.offset) to it.value }.toMap().toSortedMap()
    one.copy(map = events)
  }
  return splitDmList
}

private fun List<DurationMap>.putEmptyMarkers(): List<DurationMap> {
  val initialised = listOf(DurationMap(TimeSignature(4, 4))).plus(this)
  return initialised.windowed(2).map { (one, two) ->
    one.map.toList().lastOrNull()?.let { last ->
      val overlap = last.first + last.second.duration - one.timeSignature.duration
      if (overlap > dZero()) {
        val marker = DEvent(overlap, DurationType.NONE)
        two.copy(map = two.map.plus(dZero() to marker).toSortedMap())
      } else {
        two
      }
    } ?: two
  }
}

private fun TimeSignature.split(): List<Pair<Offset, TimeSignature>> {
  return if (numerator % 2 == 0 || numerator % 3 == 0) {
    val div = getDivisor()
    (0 until div).map { (duration.divide(div) * it) to divide(div) }
  } else if (numerator == 1) {
    try {
      listOf(
        dZero() to TimeSignature(1, denominator * 2),
        Duration(1, denominator * 2) to TimeSignature(1, denominator * 2)
      )
    } catch (e: Exception) {
      println("Stop here")
      return listOf()
    }
  } else {
    val first = TimeSignature(numerator / 2 + 1, denominator)
    val second = TimeSignature(numerator / 2, denominator)
    listOf(dZero() to first, first.duration to second)
  }
}

private fun TimeSignature.getDivisor(): Int {
  return when {
    numerator == 3 -> 3
    numerator % 3 == 0 -> numerator / 3
    else -> 2
  }
}

private fun List<DurationMap>.merge(timeSignature: TimeSignature): DurationMap {
  val offsetsRestored = map { dm ->
    val events =
      dm.map.filterNot { it.value.type == DurationType.NONE }.map { it.key + dm.offset to it.value }
        .toMap().toSortedMap()
    dm.copy(map = events)
  }
  return offsetsRestored.reduce { dm1, dm2 ->
    DurationMap(
      timeSignature,
      dm1.map.plus(dm2.map).toSortedMap()
    )
  }
}

private fun DurationMap.removeOverlapping(duration: Duration, offset: Offset): DurationMap {
  val newMap = map.filterNot { it.key >= offset && it.key < offset + duration }.toSortedMap()
  return DurationMap(timeSignature, newMap)
}

fun DurationMap.eventString(): String {
  return map.toList()
    .joinToString(separator = "")
    { it.second.asString() + ":" }.dropLastWhile { it == ':' }
}

fun DEvent.asString(): String {
  return "${getLetter()}${if (duration.numerator == 1) "" else "${duration.numerator}/"}${duration.denominator}"
}

fun DEvent.getLetter(): String {

  return when (type) {
    DurationType.CHORD -> "C"
    DurationType.REST -> "R"
    DurationType.EMPTY -> "E"
    DurationType.TUPLET_MARKER -> "T"
    else -> ""
  }
}

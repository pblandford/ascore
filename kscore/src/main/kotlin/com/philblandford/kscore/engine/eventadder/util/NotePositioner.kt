package com.philblandford.kscore.engine.eventadder.util

import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.duration.chord
import com.philblandford.kscore.engine.pitch.getClef
import com.philblandford.kscore.engine.pitch.getNotePosition
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.log.ksLogv
import kotlin.math.abs

private typealias Cluster = MutableList<Int>

fun setAllPositions(chordEvent: Event, clef: ClefType, voice:Int? = null, octaveShift: Int = 0,
                    grace: Boolean = false): Event {
  return chord(chordEvent)?.let { chord ->
    val yPositions =
      chord.notes.map { getYPosition(it.pitch, clef, octaveShift, it.position) ?: Coord() }.toList().sortedBy { it.y }
    val up = getStemDirection(yPositions, voice, grace)
    val xPositions = getXPositions(yPositions, up).toList().sortedBy { it.y }
    val notes = chord.notes.withIndex()
      .map { it.value.copy(position = xPositions.getOrNull(it.index) ?: Coord()) }
    chord.copy(notes = notes).toEvent()
  } ?: chordEvent
}

fun getYPosition(
  pitch: Pitch,
  clefType: ClefType,
  octaveShift: Int = 0,
  position: Coord = Coord()
): Coord? {
  if (pitch == unPitched()) {
    return position
  }
  val adjusted = pitch.copy(octave = pitch.octave + octaveShift)
  return getClef(clefType)?.let { clef ->
    val topPitch = Pitch(clef.topNote, Accidental.NATURAL, clef.topNoteOctave)
    getNotePosition(topPitch, adjusted)
  }?.let { Coord(0, it) }
}

fun setXPositions(chordEvent: Event):Event {
  return chord(chordEvent)?.let { chord ->
    val positions = getXPositions(chord.notes.map { it.position }.sortedBy{it.y}, chord.upstem()).toList()
    val notes = chord.notes.withIndex().map { iv ->
      if (iv.index >= positions.size) {
        ksLogv("STOP HERE")
      }
      iv.value.copy(position = positions[iv.index])
    }
    chord.copy(notes = notes).toEvent()
  } ?: chordEvent
}

fun getXPositions(yPositions: Iterable<Coord>, up: Boolean, voice:Int = 1): List<Coord> {

  val clusters = getClusters(yPositions.map { it.y })
  val defaultPositions =
    yPositions.filter { pos -> clusters.find { it.contains(pos.y) }.isNullOrEmpty() }
  val clusterPositions = clusters.flatMap { setClusterPositions(it, up, voice) }
  return defaultPositions.plus(clusterPositions).sortedBy { it.y }
}

fun setStemDirection(chord: Event, eventAddress: EventAddress, scoreQuery: ScoreQuery?): Event {
  val numVoices = scoreQuery?.numVoicesAt(eventAddress) ?: 1
  return setStemDirection(chord, numVoices, eventAddress.voice, eventAddress.isGrace)
}

fun setStemDirection(chord: Event, numVoices: Int, voiceNum: Int, grace:Boolean): Event {
  val up = if (numVoices > 1) {
    voiceNum % 2 == 1
  } else {
    val positions = chord.params.g<Iterable<Event>>(EventParam.NOTES)?.mapNotNull {
      it.params.g<Coord>(EventParam.POSITION)
    } ?: listOf()
    getStemDirection(positions, if (numVoices > 1) voiceNum else null, grace)
  }
  return chord.setModValue(EventParam.IS_UPSTEM, up)
}

fun getStemDirection(positions: Iterable<Coord>, voice:Int? = null, grace:Boolean = false): Boolean {
  voice?.let { return it == 1 }
  val grouped = positions.groupBy { it.y <= 4 }
  val downScore = grouped[true]?.fold(0) { t, v -> t + abs(v.y - 4) } ?: 0
  val upScore = grouped[false]?.fold(0) { t, v -> t + abs(v.y - 4) } ?: 0

  val ret = upScore > downScore
  return if (grace) !ret else ret
}

private fun setClusterPositions(cluster: Cluster, up: Boolean, voice:Int): Iterable<Coord> {
  val offset = if (up) 1 else -1
  var currentPos = if (up) 0 else offset
  return cluster.map {
    currentPos = if (currentPos == 0) offset else 0
    Coord(currentPos, it)
  }
}

private fun getClusters(yPositions: Iterable<Int>): Iterable<Cluster> {
  var current: Cluster? = null
  val clusters = mutableListOf<Cluster>()

  var lastNote: Int? = null

  yPositions.forEach { pos ->
    lastNote?.let { last ->
      if (pos - last <= 1) {
        if (current == null) {
          val newCluster = mutableListOf(last, pos)
          clusters.add(newCluster)
          current = newCluster
        } else {
          current?.add(pos)
        }
      } else {
        current = null
      }
    }
    lastNote = pos
  }
  return clusters
}
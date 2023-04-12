package com.philblandford.kscore.engine.core.areadirectory.segment

import com.philblandford.kscore.engine.core.representation.SEMIBREVE_WIDTH
import com.philblandford.kscore.engine.core.representation.TADPOLE_WIDTH
import com.philblandford.kscore.engine.core.representation.VOICE_CONFLICT_GAP
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.types.Event
import kotlin.math.abs

enum class VoiceRelation {
  CONFLICT, NO_CONFLICT, INTERTWINED, CONFLICT_INTERTWINED
}

fun positionAreas(eventMap: Map<Int, Event>): HashMap<Int, Int> {
  if (eventMap.size == 1) {
    return hashMapOf(1 to 0)
  } else {
    return eventMap[1]?.let { first ->
      eventMap[2]?.let { second ->
        positionPair(first, second)
      }
    } ?: hashMapOf()
  }
}

private fun positionPair(p1: Event, p2: Event): HashMap<Int, Int> {
  return when (getRelation(p1, p2)) {
    VoiceRelation.NO_CONFLICT -> positionNoConflict()
    VoiceRelation.CONFLICT -> positionConflict(p1)
    VoiceRelation.INTERTWINED -> positionIntertwined()
    VoiceRelation.CONFLICT_INTERTWINED -> positionConflictIntertwined()
  }
}

private fun positionNoConflict(): HashMap<Int, Int> {
  return hashMapOf(1 to 0, 2 to 0)
}

private fun positionConflict(p1: Event): HashMap<Int, Int> {
  return hashMapOf(1 to 0, 2 to headWidth(p1))
}

private fun headWidth(processedChord: Event): Int {
  val duration = processedChord.duration()
  return if (duration >= semibreve()) SEMIBREVE_WIDTH
  else TADPOLE_WIDTH
}

private fun positionIntertwined(): HashMap<Int, Int> {
  return hashMapOf(1 to TADPOLE_WIDTH / 2, 2 to 0)
}

private fun positionConflictIntertwined(): HashMap<Int, Int> {
  return hashMapOf(1 to (TADPOLE_WIDTH + VOICE_CONFLICT_GAP), 2 to 0)
}

private fun getRelation(p1: Event, p2: Event): VoiceRelation {
  return chord(p1)?.let { c1 ->
    chord(p2)?.let { c2 ->
      getVoiceRelation(c1.notes, c2.notes)
    }
  } ?: VoiceRelation.NO_CONFLICT
}

 fun getVoiceRelation(positions1: Iterable<Note>, positions2: Iterable<Note>): VoiceRelation {
  return if (positions1.count() == 0 || positions2.count() == 0) VoiceRelation.NO_CONFLICT
  else {
    val intertwined = getIntertwined(positions1, positions2)
    val conflict = getPos1Clash(positions1, positions2, intertwined)

    when (Pair(conflict, intertwined)) {
      Pair(true, true) -> VoiceRelation.CONFLICT_INTERTWINED
      Pair(true, false) -> VoiceRelation.CONFLICT
      Pair(false, true) -> VoiceRelation.INTERTWINED
      Pair(false, false) -> VoiceRelation.NO_CONFLICT
      else -> VoiceRelation.NO_CONFLICT
    }
  }
}

private fun getPos1Clash(list1: Iterable<Note>, list2: Iterable<Note>, intertwined: Boolean): Boolean {

  fun test(diff: Int, duration1: Duration, duration2: Duration): Boolean {
    return if (intertwined) diff <= 1
    else {
      if (diff == 0) {
        duration1 >= semibreve() || duration2 >= semibreve() ||
            headType(duration1) != headType(duration2)
      } else
        diff == 1
    }
  }

  val pos1s = list1.plus(list2).filter { it.position.x == 0 }.sortedBy { it.position.y }

  return pos1s.windowed(2).any {
    it.size > 1 &&
        test(
          abs(it[1].position.y - it[0].position.y),
          it[0].duration, it[1].duration
        )
  }
}

private fun headType(duration: Duration): Duration {
  val undotted = duration.undot()
  return if (undotted < crotchet()) crotchet() else undotted
}

private fun getIntertwined(list1: Iterable<Note>, list2: Iterable<Note>): Boolean {
  return !list1.any { it.duration >= semibreve() } &&
      !list2.any { it.duration >= semibreve() } &&
      list1.last().position.y > list2.first().position.y
}

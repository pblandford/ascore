package com.philblandford.kscore.clipboard

import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.duration.Offset
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.map.EMK
import com.philblandford.kscore.engine.map.EventHash
import com.philblandford.kscore.engine.eventadder.*
import com.philblandford.kscore.engine.pitch.Transposition
import com.philblandford.kscore.engine.pitch.keyDistance
import com.philblandford.kscore.engine.pitch.transpose
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.util.lastKeyOrNull
import com.philblandford.kscore.log.ksLogd
import java.util.*

typealias SelectionRange = SortedMap<Duration, EventHash>

data class Selection(val startAddress: EventAddress, val range: SelectionRange)

private val copiable = listOf(
  EventType.TUPLET, EventType.DURATION, EventType.DYNAMIC,
  EventType.GLISSANDO, EventType.EXPRESSION_TEXT, EventType.LYRIC, EventType.HARMONY,
  EventType.REPEAT_BAR
)

val priorities = copiable.withIndex().map { it.value to it.index }.toMap()

private fun sortEvents(eventHash: EventHash): EventHash {
  return eventHash.toList().sortedBy { priorities[it.first.eventType] ?: 0 }.toMap()
}


class Clipboard {

  private var selection: Selection? = null
  private var cut: Boolean = false

  fun copy(start: EventAddress, end: EventAddress, score: ScoreQuery) {

    ksLogd("Copying $start to $end")

    val startVoice = if (start.voice == 0) start.copy(voice = INT_WILD) else start
    val endVoice = if (start.voice == 0) end.copy(voice = INT_WILD) else end

    val grouped = score.collateEvents(copiable, startVoice, endVoice)?.let { events ->
      events.toList().groupBy {
        score.getDuration(start, it.first.eventAddress) ?: dZero()
      }
    }?.map { it.key to it.value.toMap() }
    grouped?.let { selection = Selection(start, it.toMap().toSortedMap()) }
    cut = false
  }

  fun cut(start: EventAddress, end: EventAddress, score: Score) {
    copy(start, end, score)
    cut = true
  }

  fun paste(start: EventAddress, score: Score): ScoreResult {
    ksLogd("Pasting at $start")

    score.apply {

      if (score.getEventAt(EventType.TUPLET, start) != null
        && score.getEvent(EventType.TUPLET, start) == null) {
        return asError("Cannot paste into the middle of a tuplet")
      }

      val ret = selection.ifNullWarn(score, "No selection") { sel ->
        val staveShift = getStaveShift(sel.startAddress.staveId, start.staveId)
        getKeyMap().then { keys ->
          addExtraBars(start)
            .then { (if (cut) it.removeSelection(sel) else it.ok()) }
            .then { it.pasteEvents(sel, start, staveShift, keys) }
        }
      }
      cut = false
      return ret

    }
  }

  private fun Score.getKeyMap(): AnyResult<Map<PartNum, Int>> {
    return getAllStaves(true).map { it.main }.distinct()
      .mapOrFail { part ->
        val address = ez(1).copy(staveId = StaveId(part, 0))
        getKeySignature(address).ifNullError("Could not get key signature for part $part") {
          (part to it).ok()
        }
      }.then { it.toMap().ok() }
  }


  private fun Score.pasteEvents(
    selection: Selection,
    start: EventAddress,
    staveShift: Int,
    keys: Map<PartNum, Int>
  ): ScoreResult {
    return fold(selection.range.toList()) { (offset, events) ->
      pasteEvents(start, offset, staveShift, keys, events)
    }
  }

  private fun Score.pasteEvents(
    start: EventAddress,
    offset: Offset,
    staveShift: Int,
    keys: Map<PartNum, Int>,
    events: EventHash
  ): ScoreResult {
    val sorted = sortEvents(events)

    return addDuration(start, offset)?.let { target ->
      fold(sorted.toList()) { (emk, ev) ->

        pasteEvent(emk, ev, staveShift, keys, target)
      }
    } ?: Warning(NotFound("Could not add duration $offset to $start"), this)
  }

  private fun Score.getTransposition(
    start: StaveId,
    target: StaveId,
    keys: Map<PartNum, Int>
  ): Transposition? {
    return if (getOption<Boolean>(EventParam.OPTION_SHOW_TRANSPOSE_CONCERT) != true) {
      keys[target.main]?.let { targetKs ->
        keys[start.main]?.let { startKs ->
          if (startKs != targetKs) {
            val shift = keyDistance(startKs, targetKs)
            val accidental = if (targetKs >= 0) Accidental.SHARP else Accidental.FLAT
            Transposition(startKs, shift, accidental)
          } else null
        }
      }
    } else null
  }

  private fun Score.pasteEvent(
    emk: EMK,
    event: Event,
    staveShift: Int,
    keys: Map<PartNum, Int>,
    target: EventAddress
  ): ScoreResult {

    return getStaveTarget(emk.eventAddress.staveId, staveShift)?.let { targetStave ->
      val transposition = getTransposition(
        emk.eventAddress.staveId,
        targetStave,
        keys
      )
      val address = target.copy(
        voice = emk.eventAddress.voice,
        graceOffset = emk.eventAddress.graceOffset,
        staveId = targetStave
      )
      val transposed = transposition?.let { event.transpose(it) } ?: event
      NewEventAdder.addEvent(this, transposed.eventType, transposed.params, address)
    } ?: Warning(NotFound("Could not get Stave Target"), this)
  }

  private fun Score.addExtraBars(pasteStart: EventAddress): ScoreResult {
    return selection?.range?.lastKeyOrNull()?.let { lastOffset ->
      addDuration(pasteStart, lastOffset)?.let { endPaste ->
        val extra = endPaste.barNum - numBars
        if (extra > 0) {
          NewEventAdder.addEvent(
            this,
            EventType.BAR,
            paramMapOf(EventParam.NUMBER to extra),
            ez(numBars)
          )
        } else Right(this)
      }
    } ?: Right(this)
  }

  private fun Score.getStaveShift(start: StaveId, target: StaveId): Int {
    val allStaveIds = getAllStaves(true)
    val startIdx = allStaveIds.indexOf(start)
    val endIdx = allStaveIds.indexOf(target)
    if (startIdx != -1 && endIdx != -1) {
      return endIdx - startIdx
    } else {
      return 0
    }
  }

  private fun Score.getStaveTarget(start: StaveId, shift: Int): StaveId? {
    val allStaveIds = getAllStaves(true)
    val idx = allStaveIds.indexOf(start)
    if (idx != -1) {
      return allStaveIds.toList().getOrNull(idx + shift)
    } else {
      return null
    }
  }

  private fun Score.removeSelection(selection: Selection): ScoreResult {
    return fold(selection.range.toList()) { (offset, events) ->
      selection.range
      addDuration(selection.startAddress, offset)?.let { target ->
        fold(events.toList()) { (emk, ev) ->
          NewEventAdder.deleteEvent(this, ev.eventType, paramMapOf(), emk.eventAddress)
        }
      } ?: Warning(Error("Could not add offset $offset to ${selection.startAddress}"), this)
    }
  }

  companion object {
    private val clipboard: Clipboard = Clipboard()

    fun copy(start: EventAddress, end: EventAddress, score: Score) {
      clipboard.copy(start, end, score)
    }

    fun cut(start: EventAddress, end: EventAddress, score: Score) {
      clipboard.cut(start, end, score)
    }

    fun paste(start: EventAddress, score: Score): ScoreResult {
      //ScoreContainer.instance.push()
      return clipboard.paste(start, score)
    }
  }
}
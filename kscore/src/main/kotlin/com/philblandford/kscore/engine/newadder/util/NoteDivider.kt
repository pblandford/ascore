package com.philblandford.kscore.engine.newadder.util

import com.philblandford.kscore.engine.core.score.Tuplet
import com.philblandford.kscore.engine.core.score.tuplet
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.duration.duration
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.types.*


internal fun divideNote(happening: Happening, scoreQuery: ScoreQuery?): Iterable<Happening> {

  return divideNote(happening.event, happening.eventAddress,
    {
      scoreQuery?.getEventAt(
        EventType.TUPLET,
        it.copy(staveId = happening.originalAddress.staveId)
      )?.let { tuplet(it.second, it.first.eventAddress.offset) }
    }
  ) {
    scoreQuery?.getTimeSignature(it) ?: TimeSignature(4, 4)
  }.map {
    Happening(
      it.key,
      it.value,
      true,
      originalAddress = it.key.copy(staveId = happening.originalAddress.staveId)
    )
  }
}

internal fun divideNote(
  event: Event, startAddress: EventAddress,
  tupletAt: (EventAddress) -> Tuplet?,
  getTs: (EventAddress) -> TimeSignature
): Map<EventAddress, Event> {
  val notes = mutableMapOf<EventAddress, Event>()
  var divideState = DivideState(event.duration(), event, dZero(), startAddress, startAddress, false)

  while (divideState.remainder > dZero()) {
    val ts = getTs(divideState.address)

    val address = divideState.address
    tupletAt(divideState.address)?.let { tuplet ->
      val endTuplet = tuplet.offset.add(tuplet.realDuration)
      val leftInTupletReal =
        tuplet.offset.add(tuplet.realDuration).subtract(divideState.address.offset)
      val leftInTupletWritten = tuplet.realToWritten(leftInTupletReal)
      val nextAddress =
        if (endTuplet >= ts.duration) divideState.address.inc().copy(offset = dZero()) else divideState.address.copy(
          offset = endTuplet
        )
      divideState = getNewState(leftInTupletWritten, nextAddress, divideState) {
        tuplet.writtenToReal(it)
      }
    } ?: run {
      val leftInSection = ts.duration.subtract(divideState.address.offset)
      divideState =
        getNewState(leftInSection, divideState.address.inc().copy(offset = dZero()), divideState)
    }
    notes.put(address, divideState.note)
  }
  return notes
}

private data class DivideState(
  val remainder: Duration, val note: Event,
  val lastDuration: Duration,
  val address: EventAddress, val startAddress: EventAddress, val isTied: Boolean
)

private fun getNewState(
  leftInSection: Duration, nextAddress: EventAddress, divideState: DivideState,
  getRealDuration: (Duration) -> Duration = { it }
): DivideState {

  val thisNote =
    if (divideState.remainder <= leftInSection) divideState.remainder else leftInSection
  val remainder =
    if (divideState.remainder <= leftInSection) dZero() else divideState.remainder.subtract(
      leftInSection
    )
  val realDuration = getRealDuration(thisNote)
  var newEvent =
    divideState.note.addParam(EventParam.DURATION, thisNote)
      .addParam(EventParam.REAL_DURATION, realDuration)

  var isTied = divideState.isTied
  if (divideState.address == divideState.startAddress && remainder > dZero()) {
    isTied = true
    newEvent = newEvent.addParam(EventParam.IS_START_TIE, true)
  } else if (isTied) {
    if (remainder == dZero()) {
      newEvent = newEvent.removeParam(EventParam.IS_START_TIE)
    }
    newEvent = newEvent.addParam(EventParam.IS_END_TIE, true)
      .addParam(EventParam.END_TIE, divideState.lastDuration)
  }
  val lastDuration = newEvent.duration()
  return DivideState(
    remainder,
    newEvent,
    lastDuration,
    nextAddress,
    divideState.startAddress,
    isTied
  )
}
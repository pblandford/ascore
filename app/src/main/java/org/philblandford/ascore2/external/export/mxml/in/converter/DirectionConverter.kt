package com.philblandford.ascore.external.export.mxml.`in`.converter

import com.philblandford.ascore.external.export.mxml.out.*
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.duration.dot
import com.philblandford.kscore.engine.tempo.Tempo
import com.philblandford.kscore.engine.types.*

internal fun convertDirection(
  mxmlDirection: MxmlDirection,
  measureState: MeasureState
): MeasureState {

  val above = mxmlDirection.placement != "below"
  val staff = mxmlDirection.staff?.num ?: 1

  convertExpressionDash(mxmlDirection, above, staff, measureState)?.let { return it }

  return mxmlDirection.directionType.firstOrNull()?.content?.let { content ->
    when (content) {
      is MxmlWords -> convertWords(content, measureState, above, staff)
      is MxmlMetronome -> convertMetronome(content, measureState)
      is MxmlRehearsal -> convertRehearsal(content, measureState)
      is MxmlDynamics -> convertDynamics(content, measureState, above, staff)
      is MxmlWedge -> convertWedge(content, measureState, above, staff)
      is MxmlOctaveShift -> convertOctaveShift(content, measureState, staff)
      is MxmlPedal -> convertPedal(content, measureState, staff)
      else -> measureState
    }

  } ?: measureState
}

private fun convertExpressionDash(
  mxmlDirection: MxmlDirection, above: Boolean, staff: Int,
  measureState: MeasureState
): MeasureState? {
  return mxmlDirection.directionType.find { it.content is MxmlDashes }
    ?.content?.let { it as MxmlDashes }?.let { dashes ->
    val start = dashes.type == "start"
    val words =
      mxmlDirection.directionType.find { it.content is MxmlWords }?.content?.let { it as MxmlWords }

    convertLine(
      dashes, EventType.EXPRESSION_DASH,
      measureState, above, staff,
      { !start },
      { null },
      { words?.let { paramMapOf(EventParam.TEXT to words.text) } ?: paramMapOf() }
    )
  }
}

private fun convertRehearsal(rehearsal: MxmlRehearsal, measureState: MeasureState): MeasureState {
  val scoreEvents = measureState.scoreEvents.putEvent(
    eZero(), Event(
      EventType.REHEARSAL_MARK,
      paramMapOf(EventParam.TEXT to rehearsal.text)
    )
  )
  return measureState.copy(scoreEvents = scoreEvents)
}

private fun convertMetronome(metronome: MxmlMetronome, measureState: MeasureState): MeasureState {
  return mxmlToDuration(metronome.beatUnit.text)?.let { duration ->
    val dots = metronome.beatUnitDot.count()
    val scoreEvents = measureState.scoreEvents.putEvent(
      eZero(),
      Tempo(duration.dot(dots), metronome.perMinute.num).toEvent()
    )
    measureState.copy(scoreEvents = scoreEvents)
  } ?: measureState

}

private fun convertWords(
  mxmlWords: MxmlWords,
  measureState: MeasureState,
  above: Boolean,
  staff: Int
): MeasureState {
  val id = if (above) 0 else 1
  val type =
    if (measureState.next.offset == dZero() && staff == 1 && above) EventType.TEMPO_TEXT else EventType.EXPRESSION_TEXT
  val (key, event) = if (type == EventType.EXPRESSION_TEXT) {
    EventAddress(0, measureState.next.offset, staveId = StaveId(0, staff), id = id) to
        Event(type, paramMapOf(EventParam.TEXT to mxmlWords.text, EventParam.IS_UP to above))
  } else {
    EventAddress(0, measureState.next.offset) to
        Event(type, paramMapOf(EventParam.TEXT to mxmlWords.text))
  }
  return if (type == EventType.EXPRESSION_TEXT) {
    val staveEvents = measureState.staveEvents.putEvent(key, event)
    measureState.copy(staveEvents = staveEvents)
  } else {
    val scoreEvents = measureState.scoreEvents.putEvent(key, event)
    measureState.copy(scoreEvents = scoreEvents)
  }
}

private fun convertDynamics(
  dynamics: MxmlDynamics,
  measureState: MeasureState,
  above: Boolean,
  staff: Int
): MeasureState {
  val id = if (above) 0 else 1
  val type = mxmlToDynamic(dynamics.component)
  val staveEvents = measureState.staveEvents.putEvent(
    EventAddress(0, measureState.next.offset, staveId = StaveId(0, staff), id = id),
    Event(EventType.DYNAMIC, paramMapOf(EventParam.TYPE to type, EventParam.IS_UP to above))
  )
  return measureState.copy(staveEvents = staveEvents)
}

private fun mxmlToDynamic(mxmlDynamicsComponent: MxmlDynamicsComponent): DynamicType {
  return when (mxmlDynamicsComponent) {
    is MxmlPppppp -> DynamicType.MOLTO_PIANISSIMO
    is MxmlPpppp -> DynamicType.MOLTO_PIANISSIMO
    is MxmlPppp -> DynamicType.MOLTO_PIANISSIMO
    is MxmlPpp -> DynamicType.MOLTO_PIANISSIMO
    is MxmlPp -> DynamicType.PIANISSIMO
    is MxmlP -> DynamicType.PIANO
    is MxmlFfffff -> DynamicType.MOLTO_FORTISSIMO
    is MxmlFffff -> DynamicType.MOLTO_FORTISSIMO
    is MxmlFfff -> DynamicType.MOLTO_FORTISSIMO
    is MxmlFff -> DynamicType.MOLTO_FORTISSIMO
    is MxmlFf -> DynamicType.FORTISSIMO
    is MxmlF -> DynamicType.FORTE
    is MxmlFp -> DynamicType.FORTE_PIANO
    is MxmlFz -> DynamicType.SFORZANDO
    is MxmlMf -> DynamicType.MEZZO_FORTE
    is MxmlMp -> DynamicType.MEZZO_PIANO
    is MxmlRf -> DynamicType.SFORZANDO
    is MxmlRfz -> DynamicType.SFORZANDO
    is MxmlSf -> DynamicType.SFORZANDO
    is MxmlSfz -> DynamicType.SFORZANDO
    is MxmlSffz -> DynamicType.SFORZANDISSMO
    is MxmlSfp -> DynamicType.SFORZANDO_PIANO
    is MxmlSfpp -> DynamicType.SFORZANDO_PIANO
  }
}

private fun <T : MxmlDirectionTypeContent> convertLine(
  line: T,
  eventType: EventType,
  measureState: MeasureState,
  above: Boolean?,
  staff: Int,
  isStop: (T) -> Boolean,
  getType: (T) -> Any?,
  getExtraParams: (T) -> ParamMap = { paramMapOf() },
  addEvent: (MeasureState, Event, EventAddress) -> MeasureState = { ms, ev, ea ->
    ms.copy(staveEvents = ms.staveEvents.putEvent(ea, ev))
  }
): MeasureState {
  val id = if (above == false) 1 else 0
  val stop = isStop(line)
  val offset = if (stop) measureState.current.offset else measureState.next.offset
  val address = EventAddress(0, offset, staveId = StaveId(0, staff), id = id)
  var params = paramMapOf()
  if (stop) {
    measureState.staveEvents.getEvent(eventType, address)?.let { existing ->
      params = existing.params.plus(EventParam.END to true)
    } ?: run {
      params = params.plus(EventParam.END to true)
    }
  } else {
    getType(line)?.let {
      params = params.plus(EventParam.TYPE to it)
    }
    above?.let {
      params = params.plus(EventParam.IS_UP to it)
    }
    params = params.plus(getExtraParams(line))
  }
  return addEvent(measureState, Event(eventType, params), address)
}

private fun convertWedge(
  wedge: MxmlWedge,
  measureState: MeasureState,
  above: Boolean,
  staff: Int
): MeasureState {

  return convertLine(
    wedge, EventType.WEDGE,
    measureState, above, staff,
    { it.type == "stop" },
    {
      when (it.type) {
        "diminuendo" -> WedgeType.DIMINUENDO
        else -> WedgeType.CRESCENDO
      }
    }
  )
}

private fun convertOctaveShift(
  octaveShift: MxmlOctaveShift,
  measureState: MeasureState,
  staff: Int
): MeasureState {

  val number = getOctaveNumber(octaveShift)

  val ms = convertLine(
    octaveShift, EventType.OCTAVE,
    measureState, null, staff,
    { it.type == "stop" },
    { null },
    {
      paramMapOf(EventParam.NUMBER to number, EventParam.IS_UP to (number > 0))
    }
  )
  val newShift = if (octaveShift.type == "stop") 0 else number
  return ms.copy(
    attributes = ms.attributes.copy(
      octaveShifts = ms.attributes.octaveShifts.plus(
        staff to newShift
      )
    )
  )
}

private fun getOctaveNumber(mxmlOctaveShift: MxmlOctaveShift): Int {
  val mNum = mxmlOctaveShift.size ?: 8
  val up = mxmlOctaveShift.type == "down" // sic
  val num = when (mNum) {
    8 -> 1
    15 -> 2
    22 -> 3
    else -> 1
  }
  return if (up) num else -num
}

private fun convertPedal(pedal: MxmlPedal, measureState: MeasureState, staff:Int): MeasureState {

  return convertLine(
    pedal, EventType.PEDAL,
    measureState, null, staff,
    { it.type == "stop" },
    { if (pedal.line == "yes") PedalType.LINE else PedalType.STAR },
    { paramMapOf() },
    { ms, ev, ea ->
      ms.copy(staveEvents = ms.staveEvents.putEvent(ea, ev)) }
  )
}
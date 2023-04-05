package com.philblandford.ascore.external.export.mxml.`in`.converter

import com.philblandford.ascore.external.export.mxml.out.*
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.duration.Offset
import com.philblandford.kscore.engine.types.*


internal fun getNotations(
  mxmlNotations: MxmlNotations, offset: Offset, staff: Int,
  noteConverterReturn: NoteConverterReturn
): NoteConverterReturn {
  var copy = noteConverterReturn

  mxmlNotations.elements.forEach {
    when (it) {
      is MxmlTied -> copy = copy.copy(note = getTied(it, copy.note))
      is MxmlSlur -> copy = copy.copy(measureState = getSlur(it, copy.measureState, offset, staff))
      is MxmlGlissando -> copy =
        copy.copy(measureState = getGlissando(it, copy.measureState, offset, staff))
      is MxmlTuplet -> copy = copy.copy(measureState = getTuplet(it, offset, copy.measureState))
      is MxmlArticulations -> copy =
        copy.copy(
          chordDecorationState = getArticulations(
            it,
            noteConverterReturn.chordDecorationState
          )
        )
      is MxmlOrnaments -> copy = getOrnaments(it, copy, offset, staff)
      is MxmlArpeggiate -> copy =
        copy.copy(
          chordDecorationState = getArpeggiate(
            it,
            noteConverterReturn.chordDecorationState
          )
        )
      is MxmlTechnical -> copy =
        copy.copy(
          chordDecorationState = getFingerings(
            it,
            noteConverterReturn.chordDecorationState
          )
        )
      is MxmlFermata -> copy =
        copy.copy(measureState = getFermata(it, offset, copy.measureState))
    }
  }
  return copy
}

private fun getTied(mxmlTied: MxmlTied, note: Event): Event {
  return when (mxmlTied.type) {
    "start" -> note.addParam(EventParam.IS_START_TIE, true)
    "stop" -> note.addParam(EventParam.IS_END_TIE, true)
    "continue" -> note.addParam(EventParam.IS_START_TIE, true).addParam(EventParam.IS_END_TIE, true)
    else -> note
  }
}

private fun getSlur(
  mxmlSlur: MxmlSlur,
  measureState: MeasureState,
  offset: Duration,
  staff: Int
): MeasureState {
  val up = mxmlSlur.placement != "below"
  val end = mxmlSlur.type == "stop"
  val id = (mxmlSlur.number?.toInt() ?: 1) - 1
  val params =
    if (end) paramMapOf(EventParam.END to true) else paramMapOf(EventParam.IS_UP to up)
  val staveEvents = measureState.staveEvents.putEvent(
    ez(0, offset).copy(staveId = StaveId(0, staff), id = id),
    Event(EventType.SLUR, params)
  )
  return measureState.copy(staveEvents = staveEvents)
}

private fun getGlissando(
  mxmlGlissando: MxmlGlissando,
  measureState: MeasureState,
  offset: Duration,
  staff: Int
): MeasureState {
  val end = mxmlGlissando.type == "stop"
  val isStraight = mxmlGlissando.lineType == "solid"
  val params =
    if (end) paramMapOf(EventParam.END to true) else paramMapOf(EventParam.IS_STRAIGHT to isStraight)
  val staveEvents = measureState.staveEvents.putEvent(
    ez(0, offset).copy(staveId = StaveId(0, staff)),
    Event(EventType.GLISSANDO, params)
  )
  return measureState.copy(staveEvents = staveEvents)
}

private fun getTuplet(
  mxmlTuplet: MxmlTuplet,
  offset: Offset,
  measureState: MeasureState
): MeasureState {
  return if (mxmlTuplet.type == "start") {
    measureState.copy(tupletStart = offset)
  } else {
    measureState.copy(tupletEnd = offset)
  }
}

private fun getArticulations(
  mxmlArticulations: MxmlArticulations,
  chordDecorationState: ChordDecorationState
): ChordDecorationState {
  val articulations = mxmlArticulations.articulations.map { mxmlToArticulation(it) }
  return chordDecorationState.copy(articulations = articulations)
}

private fun mxmlToArticulation(mxmlArticulationsElement: MxmlArticulationsElement): ArticulationType {
  return when (mxmlArticulationsElement) {
    is MxmlAccent -> ArticulationType.ACCENT
    is MxmlTenuto -> ArticulationType.TENUTO
    is MxmlStaccatissimo -> ArticulationType.STACCATISSIMO
    is MxmlStaccato -> ArticulationType.STACCATO
    is MxmlStrongAccent -> ArticulationType.MARCATO
  }
}


private fun getOrnaments(
  mxmlOrnaments: MxmlOrnaments,
  noteConverterReturn: NoteConverterReturn,
  offset: Offset,
  staff: Int
): NoteConverterReturn {

  val accidentals = mxmlOrnaments.accidentalMark.map {
    it.placement.placementToParam() to it.text.toAccidental()
  }.toMap()
  return mxmlOrnaments.element.fold(noteConverterReturn) { ncr, el ->
    when (el) {
      is MxmlTremolo -> {
        val duration = mxmlToTremolo(el)
        ncr.copy(chordDecorationState = ncr.chordDecorationState.copy(tremolo = duration))
      }
      is MxmlWavyLine -> {
        val ms = getLongTrill(el, ncr.measureState, offset, staff)
        ncr.copy(measureState = ms)
      }
      else -> {
        mxmlToOrnament(el)?.let { orn ->
          var ornament =
            accidentals[EventParam.ACCIDENTAL_ABOVE]?.let { orn.copy(accidentalAbove = it) } ?: orn
          ornament =
            accidentals[EventParam.ACCIDENTAL_BELOW]?.let { ornament.copy(accidentalBelow = it) }
              ?: ornament
          if (!mxmlOrnaments.element.any { it is MxmlWavyLine }) {
            ncr.copy(chordDecorationState = ncr.chordDecorationState.copy(ornament = ornament))
          } else ncr
        } ?: ncr
      }
    }
  }
}

private fun String.placementToParam(): EventParam? {
  return when (this) {
    "above" -> EventParam.ACCIDENTAL_ABOVE
    "below" -> EventParam.ACCIDENTAL_BELOW
    else -> null
  }
}

private fun String.toAccidental(): Accidental? {
  return when (this) {
    "sharp" -> Accidental.SHARP
    "flat" -> Accidental.FLAT
    "double-sharp" -> Accidental.DOUBLE_SHARP
    "double-flat" -> Accidental.DOUBLE_FLAT
    else -> null
  }
}


private fun getLongTrill(
  mxmlWavyLine: MxmlWavyLine,
  measureState: MeasureState,
  offset: Duration,
  staff: Int
): MeasureState {
  val up = mxmlWavyLine.placement != "below"
  val end = mxmlWavyLine.type == "stop"
  val id = if (up) 0 else 11
  val params =
    if (end) paramMapOf(EventParam.END to true) else paramMapOf(EventParam.IS_UP to up)
  val staveEvents = measureState.staveEvents.putEvent(
    ez(0, offset).copy(staveId = StaveId(0, staff), id = id),
    Event(EventType.LONG_TRILL, params)
  )
  return measureState.copy(staveEvents = staveEvents)
}

private fun mxmlToTremolo(mxmlTremolo: MxmlTremolo): Duration {
  val denominator = 4 shl mxmlTremolo.beats
  return Duration(1, denominator)
}

private fun mxmlToOrnament(element: MxmlOrnamentsElement): Ornament? {
  val type = when (element) {
    is MxmlInvertedMordent -> OrnamentType.MORDENT
    is MxmlMordent -> OrnamentType.LOWER_MORDENT
    is MxmlTrillMark -> OrnamentType.TRILL
    is MxmlTurn -> OrnamentType.TURN
    else -> null
  }
  return type?.let { Ornament(it) }
}

private fun getArpeggiate(
  mxmlArpeggiate: MxmlArpeggiate,
  chordDecorationState: ChordDecorationState
): ChordDecorationState {
  return chordDecorationState.copy(arpeggio = ArpeggioType.NORMAL)
}

private fun getFingerings(
  mxmlTechnical: MxmlTechnical,
  chordDecorationState: ChordDecorationState
): ChordDecorationState {
  val fingerings = mxmlTechnical.elements.filterIsInstance<MxmlFingering>().mapNotNull { it.text.toIntOrNull() }
  return chordDecorationState.copy(fingerings = fingerings)
}

private fun getFermata(
  mxmlFermata: MxmlFermata,
  offset: Offset,
  measureState: MeasureState
): MeasureState {
  val type = when (mxmlFermata.shape) {
    "angled" -> FermataType.TRIANGLE
    "square" -> FermataType.SQUARE
    else -> FermataType.NORMAL
  }
  val scoreEvents = measureState.scoreEvents.putEvent(
    ez(0, offset), Event(
      EventType.FERMATA,
      paramMapOf(EventParam.TYPE to type)
    )
  )
  return measureState.copy(scoreEvents = scoreEvents)
}

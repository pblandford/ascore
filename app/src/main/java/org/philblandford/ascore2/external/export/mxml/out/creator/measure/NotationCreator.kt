package com.philblandford.ascore.external.export.mxml.out.creator.measure

import com.philblandford.ascore.external.export.mxml.out.*
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.newadder.subadders.ChordDecoration
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.util.highestBit

internal fun createNotations(
  note: Event, chord: Event, eventAddress: EventAddress, scoreQuery: ScoreQuery,
  firstNoteInChord: Boolean
): MxmlNotations? {
  val tied = createTied(note)
  val slurs = if (firstNoteInChord) createSlur(eventAddress, scoreQuery) else listOf()
  val glissando = if (firstNoteInChord) createGlissando(eventAddress, scoreQuery) else null
  val tuplet = if (firstNoteInChord) createTuplet(eventAddress, scoreQuery) else null
  val technicals = if (firstNoteInChord) createTechnical(chord) else null
  val articulations = if (firstNoteInChord) createArticulations(chord) else null
  val ornaments = if (firstNoteInChord) createOrnaments(chord) else null
  val longTrills = if (firstNoteInChord) createLongTrill(eventAddress, scoreQuery) else listOf()
  val arpeggiate = if (firstNoteInChord) createArpeggiate(chord) else null
  val fermata = if (firstNoteInChord) createFermata(eventAddress, scoreQuery) else null

  val elements =
    listOfNotNull(
      tied,
      glissando,
      tuplet,
      articulations,
      ornaments,
      arpeggiate,
      fermata,
      technicals
    ).plus(
      slurs
    ).plus(
      longTrills
    )

  return if (elements.isEmpty()) {
    null
  } else {
    MxmlNotations(elements)
  }
}

private fun createTied(note: Event): MxmlTied? {
  return if (note.isTrue(EventParam.IS_START_TIE) && note.isTrue(EventParam.IS_END_TIE)) {
    MxmlTied("continue")
  } else if (note.isTrue(EventParam.IS_START_TIE)) {
    MxmlTied("start")
  } else if (note.isTrue(EventParam.IS_END_TIE)) {
    MxmlTied("stop")
  } else null
}

private fun createGlissando(
  eventAddress: EventAddress,
  scoreQuery: ScoreQuery
): MxmlGlissando? {
  return scoreQuery.getEvent(EventType.GLISSANDO, eventAddress.voiceless())?.let { event ->
    val type = if (event.isTrue(EventParam.END)) "stop" else "start"
    val lineType = if (type == "start") {
      if (event.isTrue(EventParam.IS_STRAIGHT)) "solid" else "wavy"
    } else null
    MxmlGlissando(type, lineType)
  }
}

private fun createSlur(eventAddress: EventAddress, scoreQuery: ScoreQuery): Iterable<MxmlSlur> {
  return (0..1).mapNotNull { id ->
    scoreQuery.getEvent(EventType.SLUR, eventAddress.copy(voice = 0, id = id))?.let { event ->
      val type = if (event.isTrue(EventParam.END)) "stop" else "start"
      val placement = if (event.isTrue(EventParam.IS_UP)) "above" else "below"
      MxmlSlur(type, "1", placement)
    }
  }
}

private fun createTuplet(eventAddress: EventAddress, scoreQuery: ScoreQuery): MxmlTuplet? {
  return scoreQuery.getEvent(EventType.TUPLET, eventAddress)?.let { tupletEvent ->
    val type = if (tupletEvent.isTrue(EventParam.END)) "stop" else "start"
    MxmlTuplet(type)
  }
}

private fun createArticulations(note: Event): MxmlArticulations? {
  return note.getParam<ChordDecoration<ArticulationType>>(EventParam.ARTICULATION)?.let { cd ->
    val elements = cd.items.mapNotNull { articulationToMxml(it) }
    MxmlArticulations(elements)
  }
}

private fun articulationToMxml(articulationType: ArticulationType): MxmlArticulationsElement? {
  return when (articulationType) {
    ArticulationType.ACCENT -> MxmlAccent()
    ArticulationType.STACCATO -> MxmlStaccato()
    ArticulationType.TENUTO -> MxmlTenuto()
    ArticulationType.STACCATISSIMO -> MxmlStaccatissimo()
    ArticulationType.MARCATO -> MxmlStrongAccent()
  }

}

private fun createArpeggiate(note: Event): MxmlArpeggiate? {
  return note.getParam<ChordDecoration<OrnamentType>>(EventParam.ARPEGGIO)?.let { cd ->
    MxmlArpeggiate()
  }
}

private fun createOrnaments(note: Event): MxmlOrnaments? {
  var accidentalMarks:List<MxmlAccidentalMark> = listOf()
  val ornament = note.getParam<ChordDecoration<Ornament>>(EventParam.ORNAMENT)?.let { cd ->
    accidentalMarks = getAccidentalMark(cd.items.first())
    ornamentToMxml(cd.items.first().ornamentType)
  }
  val tremolo = note.getParam<ChordDecoration<Duration>>(EventParam.TREMOLO_BEATS)?.let { tb ->
    tremoloToMxml(tb.items.first())
  }
  val elements = listOfNotNull(ornament, tremolo)
  return if (elements.isNotEmpty()) {
    MxmlOrnaments(elements, accidentalMarks)
  } else null
}

private fun getAccidentalMark(ornament: Ornament):List<MxmlAccidentalMark> {
  val marks = mutableListOf<MxmlAccidentalMark>()
  ornament.accidentalAbove?.let {
    it.toMxml()?.let { s ->
      marks.add(MxmlAccidentalMark("above", s))
    }
  }
  ornament.accidentalBelow?.let {
    it.toMxml()?.let { s ->
      marks.add(MxmlAccidentalMark("below", s))
    }
  }
  return marks
}

private fun Accidental.toMxml():String? {
  return when(this) {
    Accidental.SHARP -> "sharp"
    Accidental.FLAT -> "flat"
    Accidental.DOUBLE_SHARP -> "double-sharp"
    Accidental.FORCE_FLAT -> "double-flat"
    Accidental.NATURAL -> "natural"
    else -> null
  }
}

private fun ornamentToMxml(ornament: OrnamentType): MxmlOrnamentsElement {
  return when (ornament) {
    OrnamentType.TRILL -> MxmlTrillMark()
    OrnamentType.TURN -> MxmlTurn()
    OrnamentType.MORDENT -> MxmlInvertedMordent()
    OrnamentType.LOWER_MORDENT -> MxmlMordent()
  }
}


private fun createLongTrill(eventAddress: EventAddress, scoreQuery: ScoreQuery): Iterable<MxmlOrnaments> {
  return (0..1).mapNotNull { id ->
    scoreQuery.getEvent(EventType.LONG_TRILL, eventAddress.copy(id = id))?.let { event ->
      val trillSign = ornamentToMxml(OrnamentType.TRILL)
      val placement = if (event.isTrue(EventParam.IS_UP)) "above" else "below"
      val type = if (event.isTrue(EventParam.END)) "stop" else "start"
      val wavyLine = MxmlWavyLine(type, placement)
      MxmlOrnaments(listOf(trillSign, wavyLine))
    }
  }
}

private fun tremoloToMxml(duration:Duration):MxmlTremolo {
  val beats = duration.denominator.highestBit() - 3
  return MxmlTremolo("start", beats)
}


private fun createTechnical(chord: Event): MxmlTechnical? {
  return chord.getParam<ChordDecoration<Int>>(EventParam.FINGERING)?.let { fingering ->
    val fingerings = fingering.items.map { num ->
      MxmlFingering(num.toString())
    }
    MxmlTechnical(fingerings)
  }
}

private fun createFermata(eventAddress: EventAddress, scoreQuery: ScoreQuery): MxmlFermata? {
  return scoreQuery.getEvent(EventType.FERMATA, eventAddress)?.let { fermata ->
    val shape = when (fermata.subType) {
      FermataType.TRIANGLE -> "angled"
      FermataType.SQUARE -> "square"
      else -> null
    }
    MxmlFermata(shape)
  }
}
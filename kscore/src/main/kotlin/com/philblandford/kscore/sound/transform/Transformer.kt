package com.philblandford.kscore.sound.transform

import com.philblandford.kscore.engine.duration.Chord
import com.philblandford.kscore.engine.duration.Offset
import com.philblandford.kscore.engine.duration.chord
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.types.Accidental
import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.util.replace


object Transformer {
  fun transform(
    chordEvent: Event,
    ks: Int,
    longTrillActive: Event?,
    graceNotes: Map<Offset, Chord> = mapOf(),
    getNextChord: () -> Chord? = { null }
  ): List<Pair<Offset, Event>> {

    if (transEvents.intersect(chordEvent.params.keys).isEmpty() && graceNotes.isEmpty()
      && longTrillActive == null && getNextChord() == null) {
      return listOf(dZero() to chordEvent)
    }

    if (graceNotes.isNotEmpty()) {
      GraceNoteTransformer.transform(chordEvent, graceNotes.map { it.key to it.value.toEvent() }.toMap())?.let {
        return it.map { it.key to it.value }.toList()
      }
    }

    return chord(chordEvent)?.let { chord ->

      getNextChord()?.let { nextChord ->
        return GlissandoTransformer.transform(chord, ks, nextChord)
          .map { it.first to it.second.toEvent() }
      }

      chord.arpeggio?.let {
        return ArpeggioTransformer.transform(chord).map { it.first to it.second.toEvent() }
      }

      longTrillActive?.let { lt ->
        val aAbove = lt.getParam<Accidental>(EventParam.ACCIDENTAL_ABOVE)
        return LongTrillTransformer.transform(chord, ks, aAbove)
          .map { it.first to it.second.toEvent() }
      }

      TremoloTransformer.transform(chord)?.let { map ->
        return map.map { it.key to it.value.toEvent() }.toList()
      }

      val list = OrnamentTransformer.transform(chord, ks)

      var last = list.last().first to list.last().second.copy(articulations = chord.articulations)
      last = last.first to ArticulationTransformer.transform(last.second)

      list.replace(list.size - 1, last).map { it.first to it.second.toEvent() }
    } ?: listOf(dZero() to chordEvent)

  }


  private val transEvents = setOf(
     EventParam.ARPEGGIO, EventParam.ARTICULATION,
    EventParam.ORNAMENT, EventParam.TREMOLO_BEATS
  )

}


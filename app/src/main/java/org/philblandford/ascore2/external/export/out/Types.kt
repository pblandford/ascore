package com.philblandford.ascore.external.export.out

import com.philblandford.kscore.engine.duration.Offset
import com.philblandford.kscore.sound.MidiEvent
import com.philblandford.kscore.sound.MidiPlayLookup


data class MidiTrack(val events:Iterable<Pair<Offset,MidiEvent>>)
data class MidiFile(val tracks:Iterable<MidiTrack>)

fun midiTracks(midiPlayLookup: MidiPlayLookup):Iterable<MidiTrack> {
  val allEvents = midiPlayLookup.allOffsets().flatMap {  offset ->
    midiPlayLookup.getEvents(offset)?.map { offset to it } ?: listOf()
  }

  val metaEvents = midiPlayLookup.getMetaEvents().flatMap { (key, events) ->
    events.map { key to it }
  }

  val grouped = allEvents.groupBy { it.second.channel }
  val tracks = grouped.map { MidiTrack(it.value.sortedBy { it.first }) }
  val metaTrack = MidiTrack(metaEvents)
  return listOf(metaTrack).plus(tracks)
}

fun midiFile(midiPlayLookup: MidiPlayLookup):MidiFile {
  val tracks = (midiTracks(midiPlayLookup))
  return MidiFile(tracks)
}
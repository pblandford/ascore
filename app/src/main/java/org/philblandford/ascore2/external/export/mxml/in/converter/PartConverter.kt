package com.philblandford.ascore.external.export.mxml.`in`.converter

import org.philblandford.ascore2.external.export.mxml.out.MxmlPart
import org.philblandford.ascore2.external.export.mxml.out.MxmlScorePart
import com.philblandford.kscore.api.PercussionDescr
import com.philblandford.kscore.engine.core.score.*
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.duration.minim
import com.philblandford.kscore.engine.map.emptyEventMap
import com.philblandford.kscore.engine.map.eventHashOf
import com.philblandford.kscore.engine.eventadder.util.setStemDirection
import com.philblandford.kscore.engine.eventadder.util.setXPositions
import com.philblandford.kscore.engine.types.*
import kotlin.math.min


internal fun mxmlPartToPart(
  mxmlPart: MxmlPart, mxmlScorePart: MxmlScorePart, partEventMap: EventMap,
  instruments: Map<String, Event>,
  scoreEvents: EventMap
): Pair<Part, EventMap>? {
  var ongoingAttributes = OngoingAttributes()
  var scoreEventsCopy = scoreEvents
  var partEventCopy = partEventMap
  val staveEventMaps = mutableMapOf<Int, EventMap>()
  val percussionStaves = mutableMapOf<String, PercussionDescr>()

  val getInstrument = { id: String -> instruments[id] }

  val measureReturns = mxmlPart.measures.withIndex().mapNotNull { iv ->
    val barNum = iv.index + 1

    mxmlMeasureToBar(
      iv.value,
      mxmlScorePart,
      ongoingAttributes,
      barNum,
      getInstrument
    )?.let { measureReturn ->
      partEventCopy = addTranspose(partEventCopy, ongoingAttributes, measureReturn.attributes)
      ongoingAttributes = measureReturn.attributes
      scoreEventsCopy = putScoreEvents(measureReturn, barNum, scoreEventsCopy)
      putStaveEvents(measureReturn, barNum, staveEventMaps)
      percussionStaves.putAll(measureReturn.percussionStaves)
      partEventCopy = putPartEvents(measureReturn, barNum, partEventCopy)
      barNum to measureReturn
    }
  }.toMap()

  val staves = createStaves(measureReturns, staveEventMaps, partEventMap)

  partEventCopy = setPercussionDescrs(mxmlScorePart, partEventCopy, percussionStaves)
  return Part(staves.toList(), partEventCopy) to scoreEventsCopy
}

private fun createStaves(
  measureReturns: Map<Int, MeasureReturn>,
  staveEventMaps: MutableMap<Int, EventMap>,
  partEventMap: EventMap
): Iterable<Stave> {
  val staffToBarMap = mutableMapOf<Int, Iterable<Bar>>()
  measureReturns.forEach { (_, mr) ->
    mr.bars.forEach { (staff, bar) ->
      val list = staffToBarMap[staff] ?: listOf()
      staffToBarMap.put(staff, list.plus(bar))
    }
  }
  var staves = staffToBarMap.map {
    val events = staveEventMaps[it.key] ?: emptyEventMap()
    it.key to Stave(it.value.toList(), events)
  }.map { it.second }

  staves = staves.map {
    setStemDirections(it)
  }
  staves = staves.map {
    addHarmonyPlaceHolders(it)
  }
  return staves
}

private fun addTranspose(
  partEventMap: EventMap, ongoingAttributes: OngoingAttributes,
  newAttributes: OngoingAttributes
): EventMap {
  return if (ongoingAttributes.transpose != newAttributes.transpose) {
    partEventMap.getEvent(EventType.INSTRUMENT, ez(1))?.let { instr ->
      partEventMap.putEvent(
        ez(1),
        instr.addParam(EventParam.TRANSPOSITION to newAttributes.transpose)
      )
    } ?: partEventMap
  } else {
    partEventMap
  }
}

private fun putStaveEvents(
  measureReturn: MeasureReturn,
  barNum: Int,
  staveEventMaps: MutableMap<Int, EventMap>
) {
  measureReturn.staveEvents.getAllEvents().forEach { (k, event) ->
    var staveMap = staveEventMaps[k.eventAddress.staveId.sub] ?: emptyEventMap()
    val address = EventAddress(barNum, k.eventAddress.offset, id = k.eventAddress.id)
    staveMap = staveMap.putEvent(address, event)
    staveEventMaps[k.eventAddress.staveId.sub] = staveMap
  }
}

private fun putPartEvents(
  measureReturn: MeasureReturn,
  barNum: Int,
  partEventMap: EventMap
): EventMap {
  var mapCopy = partEventMap
  measureReturn.partEvents.getAllEvents().forEach { (k, event) ->
    val address = EventAddress(barNum, k.eventAddress.offset, id = k.eventAddress.id)
    mapCopy = mapCopy.putEvent(address, event)
  }
  return mapCopy
}

private fun putScoreEvents(
  measureReturn: MeasureReturn,
  barNum: Int,
  scoreEvents: EventMap
): EventMap {
  return measureReturn.scoreEvents.getAllEvents().toList().fold(scoreEvents) { map, ev ->
    val bar = when (ev.first.eventType) {
      EventType.BREAK -> barNum - 1
      else -> barNum
    }
    map.putEvent(ev.first.eventAddress.copy(bar), ev.second)
  }
}

private fun fillGaps(bar: Bar): Bar {
  val vms = bar.voiceMaps.map { it.addEmpties() }
  return Bar(bar.timeSignature, vms, bar.eventMap)
}

private fun setStemDirections(stave: Stave): Stave {
  val newBars = stave.bars.withIndex().map { ivBar ->
    val bar = fillGaps(ivBar.value)
    val vms = bar.voiceMaps.withIndex().map { ivVm ->

      val newEm = setStemDirectionEventMap(ivVm.value.eventMap, bar.voiceNumberMap, ivVm.index + 1)
      val tuplets = ivVm.value.tuplets.map { tuplet ->
        val newTupletEm =
          setStemDirectionEventMap(tuplet.eventMap, bar.voiceNumberMap, ivVm.index + 1)
        tuplet(tuplet, newTupletEm)
      }
      try {
        voiceMap(ivVm.value.timeSignature, newEm, tuplets)
      } catch (e: Exception) {
        voiceMap()
      }
    }
    Bar(bar.timeSignature, vms, ivBar.value.eventMap)
  }
  return Stave(newBars, stave.eventMap)
}

private fun setStemDirectionEventMap(
  eventMap: EventMap, voiceNumberMap: VoiceNumberMap,
  voiceNum: Int
): EventMap {

  val hash = eventMap.getEvents(EventType.DURATION)?.map { (k, v) ->
    when (v.subType) {
      DurationType.CHORD -> {
        val numVoice = voiceNumberMap[k.eventAddress.offset] ?: 1
        var chord = setStemDirection(v, numVoice, voiceNum, false)
        chord = setXPositions(chord)
        k to chord
      }
      else -> k to v
    }
  }?.toMap() ?: eventHashOf()

  return eventMap.replaceEvents(EventType.DURATION, hash)
}

private fun setPercussionDescrs(
  mxmlScorePart: MxmlScorePart,
  partEventMap: EventMap, percussionDescrs: Map<String, PercussionDescr>
): EventMap {
  return partEventMap.getEvent(EventType.INSTRUMENT, ez(1))?.let { instrument ->
    instrument.getParam<Iterable<PercussionDescr>>(EventParam.PERCUSSION_DESC)?.let { untreatedDescrs ->
      val mutable = mutableListOf<PercussionDescr>()
      percussionDescrs.forEach { (id, perc) ->
        mxmlScorePart.midiInstrument.find { it.id == id }?.let { midiInstrument ->
          untreatedDescrs.find { it.midiId == midiInstrument.midiUnpitched?.num }?.let { descr ->
            mutable.add(perc.copy(name = descr.name))
          }
        }
      }
      val staveLines = min(mutable.size, 5)
      val adjusted = adjustStaveLines(staveLines, mutable)
      val new = instrument.addParam(EventParam.PERCUSSION_DESC, adjusted.toList()).addParam(
        EventParam.STAVE_LINES, staveLines
      )
      partEventMap.putEvent(ez(1), new)
    }
  } ?: partEventMap
}

private fun adjustStaveLines(
  totalLines: Int,
  descrs: Iterable<PercussionDescr>
): Iterable<PercussionDescr> {
  return if (totalLines == 1) {
    descrs.map { it.copy(staveLine = 4, up = false) }
  } else if (totalLines == 2) {
    descrs.map { it.copy(up = true) }
  } else {
    descrs
  }
}

private fun addHarmonyPlaceHolders(stave: Stave): Stave {
  val bars = stave.bars.map { bar ->
    addHarmonyPlaceHoldersBar(bar)
  }
  return stave.copy(bars = bars)
}

private fun addHarmonyPlaceHoldersBar(bar: Bar): Bar {
  val harmonies = bar.getEvents(EventType.HARMONY)
  return if (harmonies?.isEmpty() == false) {
    val durationEvents = bar.allVoiceEvents.toList().groupBy { it.first.eventAddress.offset }
    val placeHolders = harmonies.mapNotNull { (key, _) ->
      if (key.eventAddress.offset != dZero() && durationEvents[key.eventAddress.offset] == null) {
        key.copy(eventType = EventType.PLACE_HOLDER) to Event(
          EventType.PLACE_HOLDER,
          paramMapOf(EventParam.REAL_DURATION to minim())
        )
      } else null
    }
    if (placeHolders.isNotEmpty()) {
      bar.copy(eventMap = bar.eventMap.replaceEvents(EventType.PLACE_HOLDER, placeHolders.toMap()))
    } else {
      bar
    }
  } else {
    bar
  }
}
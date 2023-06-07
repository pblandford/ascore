package com.philblandford.ascore.external.export.mxml.out.creator.measure

import BeamStateQuery
import com.philblandford.ascore.external.export.mxml.out.creator.RepeatBarQuery
import com.philblandford.kscore.engine.core.score.Bar
import com.philblandford.kscore.engine.core.score.Tuplet
import com.philblandford.kscore.engine.core.score.VoiceMap
import com.philblandford.kscore.engine.core.score.tuplet
import com.philblandford.kscore.engine.dsl.rest
import com.philblandford.kscore.engine.duration.Offset
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.duration.duration
import com.philblandford.kscore.engine.map.EventHash
import com.philblandford.kscore.engine.map.EventMapKey
import com.philblandford.kscore.engine.map.eventHashOf
import com.philblandford.kscore.engine.types.*
import org.philblandford.ascore2.external.export.mxml.out.MxmlBackup
import org.philblandford.ascore2.external.export.mxml.out.MxmlBarStyle
import org.philblandford.ascore2.external.export.mxml.out.MxmlBarline
import org.philblandford.ascore2.external.export.mxml.out.MxmlDuration
import org.philblandford.ascore2.external.export.mxml.out.MxmlEnding
import org.philblandford.ascore2.external.export.mxml.out.MxmlMeasure
import org.philblandford.ascore2.external.export.mxml.out.MxmlMeasureElement
import org.philblandford.ascore2.external.export.mxml.out.MxmlPrint
import org.philblandford.ascore2.external.export.mxml.out.MxmlRepeat

private data class StaffVoice(val staveNum: StaveNum, val voice: Voice)

internal fun createMeasure(
  bars: Map<StaveNum, Bar>, partNum: Int, barNum: BarNum,
  currentDivisions: Int,
  getInstrumentId: (String) -> String?,
  scoreQuery: ScoreQuery,
  repeatBarQuery: RepeatBarQuery
): MxmlMeasure? {


  val durationEvents = collateVoiceEvents(bars, barNum, partNum, EventType.DURATION)
  val harmonyLookup = getHarmonyLookup(bars, barNum, partNum, durationEvents)
  val lyricEvents = collateVoiceEvents(bars, barNum, partNum, EventType.LYRIC)
  val tuplets = collateVoiceEvents(bars, barNum, partNum, EventType.TUPLET).mapNotNull { (k, v) ->
    tuplet(v)?.let { k.eventAddress.offset to it }
  }
  val beamStateQueries = createStateBeamQueries(bars)
  var divisions = currentDivisions

  val elements = mutableListOf<MxmlMeasureElement>()
  val staves = bars.map { it.key }

  var offset = dZero()

  val eventGroups = getBarVoiceEvents(bars)

  eventGroups.forEach { (sv, vm) ->
    val pair = createNotesAtOffset(
      divisions,
      EventAddress(barNum, offset, null, StaveId(partNum, sv.staveNum), sv.voice),
      vm, durationEvents, tuplets, lyricEvents, repeatBarQuery, harmonyLookup, beamStateQueries,
      getInstrumentId, staves, scoreQuery, elements
    )
    offset = pair.first
    divisions = pair.second
  }

  return MxmlMeasure(barNum.toString(), elements)
}

private fun createNotesAtOffset(
  currentDivisions: Int,
  eventAddress: EventAddress,
  vm: VoiceMap,
  durationEvents: EventHash,
  tuplets: List<Pair<Offset, Tuplet>>,
  lyricEvents: EventHash,
  repeatBarQuery: RepeatBarQuery,
  harmonyLookup: HarmonyLookup,
  beamStateQueries: Map<StaffVoice, BeamStateQuery>,
  getInstrumentId: (String) -> String?,
  staves: List<Int>,
  scoreQuery: ScoreQuery,
  elements: MutableList<MxmlMeasureElement>
): Pair<Offset, Int> {

  var divisions = currentDivisions
  var offset = eventAddress.offset
  if (offset != dZero()) {
    elements.add(MxmlBackup(MxmlDuration(offset.toMxml(divisions))))
    offset = dZero()
  }

  val staff: Int? = if (staves.size > 1) eventAddress.staveId.sub else null

  var allEvents = vm.getEvents(EventType.DURATION) ?: eventHashOf()
  if (allEvents.isEmpty()) {
    allEvents =
      eventHashOf(
        EventMapKey(
          EventType.DURATION,
          eZero().copy(voice = 1)
        ) to rest(vm.timeSignature.duration)
      )
  }

  val graceOther = allEvents.toList().partition { it.first.eventAddress.isGrace }
  val graceEvents = graceOther.first.groupBy { it.first.eventAddress.offset }
  val otherEvents = graceOther.second.toList().sortedBy { it.first.eventAddress.offset }

  otherEvents.forEach { (k, v) ->
    val current = eventAddress.copy(offset = k.eventAddress.offset)

    if (eventAddress.staveId.sub == 1 && eventAddress.voice == 1) {
      createPrint(scoreQuery, current)?.let { elements.add(it) }
      createAttributes(
        scoreQuery,
        current.voiceless(),
        staves,
        divisions,
        durationEvents,
        repeatBarQuery
      )?.let {
        elements.add(it)
        divisions = it.divisions?.num ?: divisions
      }
      createBarline(scoreQuery, current, true)?.let { elements.add(it) }
    }
    if (eventAddress.voice == 1) {
      createStartDirections(scoreQuery, current)?.let { elements.addAll(it) }
      createHarmony(harmonyLookup, current.voiceless(), divisions).forEach { elements.add(it) }
    }
    val tuplet = tuplets.toList()
      .find {
        it.first <= current.offset &&
            it.first.add(it.second.realDuration) > current.offset
      }?.second
    val instrumentId =
      scoreQuery.getParamAt<String>(EventType.INSTRUMENT, EventParam.NAME, current)
        ?.let { getInstrumentId(it) }
    val grace = graceEvents[current.offset] ?: listOf()
    val lyrics = lyricEvents.filter { it.key.eventAddress.idless() == eventAddress.copy(offset = offset) }
    val notes =
      createNotesForEvent(
        v,
        divisions,
        eventAddress.voice,
        staff,
        lyrics.values.toList(),
        tuplet,
        grace,
        current,
        instrumentId,
        beamStateQueries[StaffVoice(eventAddress.staveId.sub, eventAddress.voice)],
        scoreQuery
      ) ?: listOf()
    elements.addAll(notes)
    createEndDirections(scoreQuery, current)?.let { elements.addAll(it) }

    createBarline(scoreQuery, current, false)?.let { elements.add(it) }
    offset = offset.add(v.duration())
  }
  return Pair(offset, divisions)
}

private fun createStateBeamQueries(barMap: Map<StaveNum, Bar>): Map<StaffVoice, BeamStateQuery> {

  return mapOf()
//  return barMap.flatMap { (staff, bar) ->
//    bar.voiceMaps.withIndex().map { iv ->
//      StaffVoice(staff, iv.index + 1) to beamStateQuery(iv.value.beamMap)
//    }
//  }.toMap()
}

private fun getBarVoiceEvents(bars: Map<StaveNum, Bar>): List<Pair<StaffVoice, VoiceMap>> {
  return bars.flatMap { (staff, bar) ->
    bar.voiceMaps.withIndex().map { iv ->
      StaffVoice(staff, iv.index + 1) to iv.value
    }
  }.sortedWith(compareBy({ it.first.staveNum }, { it.first.voice }))
}

private fun collateVoiceEvents(
  bars: Map<Int, Bar>,
  barNum: Int,
  partNum: Int,
  eventType: EventType
): EventHash {

  return bars.flatMap { (sub, bar) ->
    bar.voiceMaps.withIndex().flatMap { iv ->
      iv.value.getEvents(eventType)?.map {
        it.key.copy(
          eventAddress = it.key.eventAddress.copy(
            barNum,
            staveId = StaveId(partNum, sub),
            voice = iv.index + 1
          )
        ) to it.value
      } ?: listOf()
    }
  }.toMap()
}


private fun collateBarEvents(
  bars: Map<Int, Bar>,
  barNum: Int,
  partNum: Int,
  eventType: EventType
): EventHash {
  return bars.flatMap { (sub, bar) ->
    bar.getEvents(eventType)?.map {
      it.key.copy(
        eventAddress = it.key.eventAddress.copy(
          barNum,
          staveId = StaveId(partNum, sub)
        )
      ) to it.value
    } ?: listOf()
  }.toMap()
}


private fun createBarline(
  scoreQuery: ScoreQuery, eventAddress: EventAddress,
  start: Boolean
): MxmlBarline? {

  val ending = createEnding(scoreQuery, eventAddress, start)

  if (start) {
    scoreQuery.getEvent(EventType.REPEAT_START, eventAddress)?.let { _ ->
      return MxmlBarline("left", MxmlBarStyle("heavy-light"), ending, MxmlRepeat("forward"))
    }
  } else {
    scoreQuery.getEvent(EventType.REPEAT_END, eventAddress)?.let { _ ->
      return MxmlBarline("right", MxmlBarStyle("light-heavy"), ending, MxmlRepeat("backward"))
    }
  }

  scoreQuery.getEvent(EventType.BARLINE, eventAddress)?.let { event ->
    val type = when (event.subType) {
      BarLineType.NORMAL -> "regular"
      BarLineType.DOUBLE -> "light-light"
      BarLineType.FINAL -> "light-heavy"
      else -> "regular"
    }
    return MxmlBarline("right", MxmlBarStyle(type), ending)
  }

  return ending?.let { e ->
    MxmlBarline("left", MxmlBarStyle("regular"), e)
  }
}

private fun createEnding(
  scoreQuery: ScoreQuery,
  eventAddress: EventAddress,
  start: Boolean
): MxmlEnding? {
  return scoreQuery.getEvent(EventType.VOLTA, eventAddress)?.let {
    val number = it.getParam<Int>(EventParam.NUMBER) ?: 1

    if (it.isTrue(EventParam.END) && !start) {
      MxmlEnding(number, "discontinue")
    } else if (!it.isTrue(EventParam.END) && start) {
      MxmlEnding(number, "start")
    } else null
  }
}

private fun createPrint(scoreQuery: ScoreQuery, eventAddress: EventAddress): MxmlPrint? {
  return scoreQuery.getEvent(EventType.BREAK, ez(eventAddress.barNum - 1))?.let { _ ->
    MxmlPrint("yes", null)
  }
}

private fun getHarmonyLookup(
  bars: Map<StaveNum, Bar>, barNum: Int, partNum: Int,
  durationEvents: EventHash
): HarmonyLookup {
  val harmonyEvents = collateBarEvents(bars, barNum, partNum, EventType.HARMONY)
  return HarmonyLookup(harmonyEvents, durationEvents)
}
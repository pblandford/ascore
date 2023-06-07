package com.philblandford.kscore.engine.eventadder.subadders

import com.philblandford.kscore.api.Instrument
import com.philblandford.kscore.api.instrument
import com.philblandford.kscore.engine.core.score.Part
import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.core.score.part
import com.philblandford.kscore.engine.core.score.voiceMap
import com.philblandford.kscore.engine.map.emptyEventMap
import com.philblandford.kscore.engine.eventadder.*
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.util.add

object PartSubAdder : BaseSubAdder {

  override fun addEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {

    return instrument(Event(EventType.INSTRUMENT, params))?.let {
      if (score.singlePartMode()) {
        return Warning(HarmlessFailure("Can't add parts in single part mode"), score)
      }
      score.addNewPart(it, eventAddress, params)
    } ?: run {
      score.setPartParam(eventAddress, params)
    }
  }

  private fun Score.addNewPart(
    instrument: Instrument, eventAddress: EventAddress, params: ParamMap
  ): ScoreResult {
    val list = subLevels.toMutableList()
    val idx =
      if (params.isTrue(EventParam.IS_UP)) eventAddress.staveId.main - 1 else
        eventAddress.staveId.main
    var part = part(instrument, numBars)
    part = addTimeSignatures(part, this)
    list.add(idx, part)
    return Right(Score(list, eventMap, beamDirectory))
  }

  private fun Score.setPartParam(eventAddress: EventAddress, params: ParamMap): ScoreResult {
     return getSubLevel(eventAddress)?.let { existing ->
      val part = existing as Part
      val label = params.g<String>(EventParam.LABEL) ?: part.label
      val abbreviation = params.g<String>(EventParam.ABBREVIATION) ?: part.abbreviation
      var newParams = part.eventMap.getEvent(EventType.PART)?.params ?: paramMapOf()
      newParams =
        newParams.plus(EventParam.LABEL to label).plus(EventParam.ABBREVIATION to abbreviation)
          .plus(params)
      val newMap = part.eventMap.putEvent(eZero(), Event(EventType.PART, newParams))
      val subLevels = subLevels.minus(part)
        .add(eventAddress.staveId.main - 1, Part(part.staves, newMap))
      Right(Score(subLevels, eventMap, beamDirectory))
    } ?: Left(NotFound("Could not find part at $eventAddress"))
  }

  private fun addTimeSignatures(part: Part, scoreQuery: ScoreQuery?): Part {
    val staves = part.staves.map { stave ->
      val bars = stave.bars.withIndex().map { iv ->
        val ts = scoreQuery?.getTimeSignature(ez(iv.index + 1)) ?: TimeSignature(4, 4)
        iv.value.copy(voiceMaps = listOf(voiceMap(ts, emptyEventMap())))
      }
      stave.copy(bars = bars)
    }
    return part.copy(staves = staves)
  }

  override fun deleteEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {

    val list = score.subLevels.toMutableList()
    return if (list.size > 1 && !score.singlePartMode()) {
      list.removeAt(eventAddress.staveId.main - 1)
      Right(Score(list, score.eventMap, score.beamDirectory))
    } else {
      Right(score)
    }
  }
}
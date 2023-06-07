package com.philblandford.kscore.engine.eventadder.subadders

import com.philblandford.kscore.api.Instrument
import com.philblandford.kscore.engine.core.score.Part
import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.eventadder.*
import com.philblandford.kscore.engine.eventadder.subadders.TransposeSubAdder.transpose
import com.philblandford.kscore.engine.pitch.keyDistance
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.option.getAllDefaults


internal object OptionSubAdder : BaseSubAdder {

  override fun addEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {
    return super.addEvent(score, destination, eventType, params, eZero())
      .then { it.postAdd(score, params) }
  }

  private fun Score.postAdd(oldScore: Score, params: ParamMap): ScoreResult {
    val changed = oldScore.getChanged(params)
    return when {
      changed.contains(EventParam.OPTION_SHOW_TRANSPOSE_CONCERT) -> {
        transposeParts(params.isTrue(EventParam.OPTION_SHOW_TRANSPOSE_CONCERT), oldScore)
      }
      else -> {
        Right(this)
      }
    }
  }

  private fun Score.transposeParts(showConcert: Boolean, originalScore: ScoreQuery): ScoreResult {
    val parts = parts.withIndex().mapOrFail { iv ->
      val part = iv.value
      val partNum = iv.index + 1
      val from = getKeySignature(eas(1, partNum, 0), !showConcert) ?: 0
      val to = getKeySignature(eas(1, partNum, 0), showConcert) ?: 0
      getInstrument(eas(1, partNum, 0), false)?.let { instrument ->
        part.transpose(from, to, instrument, numBars, partNum, this, originalScore)
      } ?: Left(NotFound("Instrument not found"))
    }
    return parts.then { Right(Score(it, eventMap, beamDirectory)) }
  }

  private fun Part.transpose(
    from: Int, to: Int, instrument: Instrument,
    numBars: Int, partNum: Int, scoreQuery: ScoreQuery, originalScore: ScoreQuery
  ): PartResult {
    val shift = keyDistance(from, to)
    return if (instrument.transposition != 0) {
      transformBars(1, numBars, partNum) { _, _, barNum, staveId ->
        this.transpose(
          { originalScore.getKeySignature(eas(barNum, staveId = staveId)) ?: 0 },
          {
            scoreQuery.getEventAt(
              EventType.CLEF,
              eas(barNum, it, staveId)
            )?.second?.subType as ClefType
          }, { scoreQuery.getOctaveShift(eas(barNum, it, staveId)) },
          shift,
          if (to >= 0) Accidental.SHARP else Accidental.FLAT
        )
      }
    } else {
      Right(this)
    }
  }

  private fun Score.getChanged(params: ParamMap): Iterable<EventParam> {
    return getEvent(EventType.OPTION, eZero())?.params?.let { existing ->
      getAllDefaults().filter {
        existing[it.key] != params[it.key]
      }
    }?.keys ?: listOf()
  }

  override fun <T> setParam(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    param: EventParam,
    value: T,
    eventAddress: EventAddress
  ): ScoreResult {
    return if (param == EventParam.OPTION_LYRIC_POSITIONS) {
      val realValue = when (value) {
        is Pair<*, *> -> {
          addLyricPosition(score, value.first as Int, value.second as Boolean?)
        }
        else -> value
      }
      super.setParam(score, destination, eventType, param, realValue, eventAddress)
    } else if (param == EventParam.OPTION_LYRIC_OFFSET_BY_POSITION) {
      val realValue = when (value) {
        is Pair<*, *> -> {
          addLyricOffsetByPosition(score, value.first as Boolean, value.second as Int)
        }
        else -> value
      }
      super.setParam(score, destination, eventType, param, realValue, eventAddress)
    } else {
      super.setParam(score, destination, eventType, param, value, eventAddress)
    }
  }

  private fun addLyricPosition(score: Score, key: Int, value: Boolean?): List<Pair<Int, Boolean>> {
    val map = score.getOption<List<Pair<Int, Boolean>>>(EventParam.OPTION_LYRIC_POSITIONS)?.toMap() ?: mapOf()
    val position = when (value) {
      null -> !(map[key] ?: false)
      else -> value
    }
    return (map + (key to position)).toList()
  }

  private fun addLyricOffsetByPosition(score: Score, key: Boolean, value: Int): List<Pair<Boolean, Int>> {

    val map = when (val existing =
      score.getOption<Any>(EventParam.OPTION_LYRIC_OFFSET_BY_POSITION)) {
      is List<*> -> {
        (existing as? List<Pair<Boolean, Int>>)?.toMap() ?: mapOf()
      }
      else -> {
        mapOf()
      }
    }
    return (map + (key to value)).toList()
  }

  override fun deleteEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {
    return Right(score)
  }
}


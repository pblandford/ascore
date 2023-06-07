package com.philblandford.kscore.engine.eventadder.subadders

import com.philblandford.kscore.api.Instrument
import com.philblandford.kscore.api.instrument
import com.philblandford.kscore.engine.core.score.Part
import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.eventadder.*
import com.philblandford.kscore.engine.eventadder.subadders.TransposeSubAdder.transpose
import com.philblandford.kscore.engine.pitch.transposeKey
import com.philblandford.kscore.engine.types.*

object InstrumentSubAdder : BaseSubAdder {
  override fun addEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {
    return instrument(params)?.let { instrument ->
      val dest = if (params.isTrue(EventParam.FOR_STAVE)) staveDestination else partDestination
      super.addEvent(score, dest, eventType, params.minus(EventParam.FOR_STAVE), eventAddress)
        .then {
          if (dest == partDestination) {
            PartSubAdder.setParam(
              it,
              partDestination,
              EventType.PART,
              EventParam.LABEL,
              instrument.label,
              eventAddress
            ).then { s ->
              PartSubAdder.setParam(
                s,
                partDestination,
                EventType.PART,
                EventParam.ABBREVIATION,
                instrument.abbreviation,
                eventAddress
              )
            }.then { s ->
              if (s.getOption<Boolean>(EventParam.OPTION_SHOW_TRANSPOSE_CONCERT) == false) {
                score.getInstrument(eventAddress, false)?.let { oldInstrument ->
                  s.transposeParts(eventAddress, oldInstrument, instrument)
                } ?: s.ok()
              } else {
                s.ok()
              }
            }
          } else Right(it)
        }
    } ?: Left(Error("Could note create instrument from params"))
  }


  private fun Score.transposeParts(
    eventAddress: EventAddress,
    oldInstrument: Instrument,
    newInstrument: Instrument
  ): ScoreResult {
    val partNum = eventAddress.staveId.main
    return getPart(partNum).ifNullFail("Part not found").then { part ->
      val ks = getKeySignature(eventAddress, true) ?: 0
      val shift = oldInstrument.transposition - newInstrument.transposition
      val to = transposeKey(ks, shift)
      part.transpose(shift, to, numBars, partNum, this).then { transposed ->
        replaceSubLevel(transposed, partNum).ok()
      }
    }
  }

  private fun Part.transpose(
    shift: Int, to: Int,
    numBars: Int, partNum: Int, scoreQuery: ScoreQuery
  ): PartResult {
    return transformBars(1, numBars, partNum) { _, _, barNum, staveId ->
      this.transpose(
        { scoreQuery.getKeySignature(ez(barNum)) ?: 0 },
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
  }

}


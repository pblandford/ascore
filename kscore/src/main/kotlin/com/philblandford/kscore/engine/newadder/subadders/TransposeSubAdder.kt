package com.philblandford.kscore.engine.newadder.subadders

import com.philblandford.kscore.engine.core.score.*
import com.philblandford.kscore.engine.duration.Offset
import com.philblandford.kscore.engine.duration.chord
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.map.eventHashOf
import com.philblandford.kscore.engine.newadder.*
import com.philblandford.kscore.engine.newadder.util.setAllPositions
import com.philblandford.kscore.engine.newadder.util.setStemDirection
import com.philblandford.kscore.engine.pitch.Transposer
import com.philblandford.kscore.engine.pitch.harmony
import com.philblandford.kscore.engine.pitch.keyDistance
import com.philblandford.kscore.engine.pitch.transposeKey
import com.philblandford.kscore.engine.types.*


object TransposeSubAdder : NewSubAdder {

  override fun addEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {

    return addEventRange(score, destination, eventType, params, ez(1), ez(score.numBars))
  }

  override fun addEventRange(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress,
    endAddress: EventAddress
  ): ScoreResult {
    val concert = score.getOption<Boolean>(EventParam.OPTION_SHOW_TRANSPOSE_CONCERT) ?: false

    val newKs = score.getNewKs(params, concert)
    val amount = score.getAmount(params, eventAddress)
    val accidental = params.g<Accidental>(EventParam.ACCIDENTAL)

    val start = eventAddress.startBar()
    val end = score.getPreviousStaveSegment(endAddress.inc()) ?: endAddress.startBar()

    return score.transposeKeySignatures(newKs, start, end)
      .then { s ->
        val newScore =
          s.transformStaves { i, i2, staveId ->
            if (s.getInstrument(eas(1, dZero(), staveId))?.percussion == true) {
              Right(this)
            } else {
              transformBars(eventAddress, endAddress, staveId) { _, _, barNum, _ ->
                transpose(
                  { score.getKeySignature(ez(barNum), concert) ?: 0 },
                  {
                    s.getEventAt(
                      EventType.CLEF,
                      eas(barNum, it, staveId)
                    )?.second?.subType as ClefType
                  },
                  {
                    s.getOctaveShift(eas(barNum, it, staveId))
                  },
                  amount,
                  accidental
                )
              }
            }
          }
        newScore
      }
      .then { it.addStartKeySignature(newKs, start) }
      .then { it.addTerminalKeySignature(score.getKeySignature(eventAddress) ?: 0, endAddress) }
  }

  private fun Score.transposeKeySignatures(
    newKs: Int, eventAddress: EventAddress, endAddress: EventAddress?
  ): ScoreResult {
    val bar = if (eventAddress.barNum.isWild()) 1 else eventAddress.barNum
    return getKeySignature(ez(bar))?.let { currentKs ->
      val shift = keyDistance(currentKs, newKs)
      val events = eventMap.getEvents(
        EventType.KEY_SIGNATURE, eventAddress.staveless(),
        endAddress?.staveless()
      )?.map {
        val shifted = transposeKey(
          it.value.getInt(EventParam.SHARPS),
          shift,
          if (newKs > 0) Accidental.SHARP else Accidental.FLAT
        )
        it.key to it.value.addParam(EventParam.SHARPS to shifted)
      }
      events?.fold(Right(this) as ScoreResult) { sr, (k, v) ->
        sr.then {
          GenericSubAdder.addEvent(
            it, EventDestination(listOf(ScoreLevelType.SCORE)), EventType.KEY_SIGNATURE,
            v.params, k.eventAddress
          )
        }
      }
    } ?: Left(NotFound("Could not get key signature at $eventAddress"))
  }

  private fun Score.addStartKeySignature(newSharps: Int, start: EventAddress): ScoreResult {
    return if (start.barNum > 1) {
      GenericSubAdder.addEvent(
        this, EventDestination(listOf(ScoreLevelType.SCORE)), EventType.KEY_SIGNATURE,
        paramMapOf(EventParam.SHARPS to newSharps), start.startBar().staveless()
      )
    } else {
      Right(this)
    }
  }

  private fun Score.addTerminalKeySignature(oldSharps: Int, endAddress: EventAddress): ScoreResult {
    return if (endAddress.barNum < numBars) {
      GenericSubAdder.addEvent(
        this, EventDestination(listOf(ScoreLevelType.SCORE)), EventType.KEY_SIGNATURE,
        paramMapOf(EventParam.SHARPS to oldSharps), endAddress.inc()
      )
    } else {
      Right(this)
    }
  }

  private fun Score.getNewKs(params: ParamMap, concert: Boolean): Int {
    return params.g<Int>(EventParam.SHARPS) ?: run {
      params.g<Int>(EventParam.AMOUNT)?.let { amount ->
        getKeySignature(ez(1), concert)?.let { sharps ->
          val accidental = params.g<Accidental>(EventParam.ACCIDENTAL)
            ?: if (sharps > 0) Accidental.SHARP else Accidental.FLAT
          transposeKey(sharps, amount, accidental)
        }
      }
    } ?: 0
  }


  private fun EventMap.transposeHarmonies(sharps: Int, amount: Int, accidental: Accidental? = null): EventMap {
    return getEvents(EventType.HARMONY)?.let { hash ->
      hash.mapNotNull { (key, value) ->
        harmony(value)?.let { harmony ->
          key to Transposer.transposeHarmony(harmony, sharps, amount, accidental).toEvent()
        }
      }
    }?.let { newHash ->
      replaceEvents(EventType.HARMONY, newHash.toMap())
    } ?: this
  }

  private fun Score.getAmount(
    params: ParamMap, eventAddress: EventAddress
  ): Int {
    return params.g<Int>(EventParam.AMOUNT) ?: run {
      params.g<Int>(EventParam.SHARPS)?.let { newKs ->
        val bar = if (eventAddress.barNum == -1) 1 else eventAddress.barNum
        val up = params.isTrue(EventParam.IS_UP)
        getKeySignature(
          eas(bar, dZero(), eventAddress.staveId),
          !singlePartMode()
        )?.let { current ->
          keyDistance(current, newKs, up)
        }
      }
    } ?: 0
  }

  fun Bar.transpose(
    oldKs: () -> Int, clef: (Offset) -> ClefType, octaveShift: (Offset) -> Int,
    shift: Int, accidental: Accidental?
  ): BarResult {
    val voiceMaps = voiceMaps.withIndex().map { iv ->
      iv.value.transpose(
        voiceNumberMap, iv.index + 1,
        clef,
        oldKs(),
        shift,
        accidental,
        octaveShift
      )
    }
    val em = eventMap.transposeHarmonies(oldKs(), shift, accidental)
    return Right(Bar(timeSignature, voiceMaps, em))
  }

  private fun VoiceMap.transpose(
    voiceNumberMap: VoiceNumberMap, voice: Int,
    getClef: (Offset) -> ClefType, oldSharps: Int, shift: Int, accidental: Accidental?,
    octaveShift: (Offset) -> Int
  ): VoiceMap {
    val events = getEvents(EventType.DURATION)?.map { (key, event) ->
      chord(event)?.let { chord ->
        val voiceOpt = voiceNumberMap[key.eventAddress.offset]?.let { if (it > 1) voice else null }
        val newChord = Transposer.transposeChord(chord, oldSharps, shift, accidental)
        val chordEvent = setAllPositions(
          newChord.toEvent(), getClef(key.eventAddress.offset), voiceOpt,
          -octaveShift(key.eventAddress.offset), key.eventAddress.isGrace
        )
        setStemDirection(
          chordEvent, voiceNumberMap[key.eventAddress.offset] ?: 1,
          voice, key.eventAddress.isGrace
        )
      }?.let { key to it } ?: (key to event)
    }?.toMap() ?: eventHashOf()
    return replaceVoiceEvents(events)
  }

}
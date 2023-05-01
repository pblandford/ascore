package com.philblandford.kscore.engine.eventadder.subadders

import com.philblandford.kscore.engine.core.score.*
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.map.EventHash
import com.philblandford.kscore.engine.eventadder.*
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.time.timeSignature
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.log.ksLogv

object TimeSignatureSubAdder : RangeSubAdder {
  override fun addEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {
    return params.check().then { checked ->
      val (address, newParams) = score.getEventAddressParams(eventType, params, eventAddress)
      when (val res = super.addEvent(score, destination, eventType, newParams, address)) {
        is Right -> res.r.postAdd(score, address, timeSignature(Event(eventType, checked))!!)
        else -> res
      }
    }
  }

  override fun <T> setParam(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    param: EventParam,
    value: T,
    eventAddress: EventAddress
  ): ScoreResult {

    /* Are we trying to set the prevailing TS when there's an upbeat bar?
     * For now we can't set params on a hidden TS, so assume this is what we are
     * trying to do
     */
    val adjustedAddress = if (eventAddress.barNum == 1 && score.getParam<Boolean>(EventType.TIME_SIGNATURE, EventParam.HIDDEN, ez(1)) == true) {
      ez(2)
    } else {
      ez(1)
    }

    return super.setParam(score, destination, eventType, param, value, adjustedAddress)
  }

  private fun Score.getEventAddressParams(
    eventType: EventType,
    params: ParamMap, eventAddress: EventAddress
  ): Pair<EventAddress, ParamMap> {
    val address = eventAddress.startBar().staveless()
    return if (address.barNum == 1 && getParam<Boolean>(
        eventType, EventParam.HIDDEN,
        ez(1)
      ) == true && !params.isTrue(EventParam.HIDDEN)
    ) {
      address + 1 to params.plus(EventParam.HIDDEN to false)
    } else {
      address to params
    }
  }

  private fun ParamMap.check(): AnyResult<ParamMap> {
    return when (this.g<TimeSignatureType>(EventParam.TYPE) ?: TimeSignatureType.CUSTOM) {
      TimeSignatureType.CUSTOM -> {
        this.g<Int>(EventParam.NUMERATOR).ifNullError("No numerator") {
          this.g<Int>(EventParam.DENOMINATOR).ifNullError("No denominator") {
            this.ok()
          }
        }
      }
      TimeSignatureType.COMMON -> this.plus(EventParam.NUMERATOR to 4)
        .plus(EventParam.DENOMINATOR to 4).ok()
      TimeSignatureType.CUT_COMMON -> this.plus(EventParam.NUMERATOR to 2)
        .plus(EventParam.DENOMINATOR to 2).ok()
    }
  }


  override fun deleteEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {
    if (eventAddress.isStart() && eventAddress.id == 0) {
      return Warning(HarmlessFailure("Not deleting first time signature in score"), score)
    }

    return super.deleteEvent(score, destination, eventType, params, eventAddress).then { r ->
      r.getTimeSignature(eventAddress)?.let { timeSignature ->
        r.postAdd(score, eventAddress, timeSignature)
      } ?: Left(Error("No time signature left in score!"))
    }
  }



  private fun Score.postAdd(
    originalScore: Score,
    eventAddress: EventAddress,
    timeSignature: TimeSignature
  ): ScoreResult {

    val offsetEvents = originalScore.getEventsAsOffsets(eventAddress, null)

    return calculateNumBars().then { numBars ->
      removeSameLater(EventType.TIME_SIGNATURE, eventAddress) {
        timeSignature(this) == timeSignature
      }
        .then {s ->
          s.adjustLaterTimeSignatures(timeSignature, originalScore, eventAddress)
        }
        .then { s ->
          s.createNewBars(eventAddress, timeSignature, numBars, this.numBars)
        }
        .then { s ->
          s.removeOldEvents(offsetEvents)
        }
        .then { s ->
          s.readdEvents(offsetEvents)
        }

    }
  }

  private fun Score.calculateNumBars(): AnyResult<Int> {
    val tsMap = this.eventMap.getEvents(EventType.TIME_SIGNATURE)?.map { it.key.eventAddress.barNum to timeSignature(it.value)!! }?.toMap() ?: mapOf()
    val ol = offsetLookup(tsMap, this.lastOffset)
    return ol.numBars.ok()
  }


  private fun Score.getEventsAsOffsets(
    eventAddress: EventAddress,
    endAddress: EventAddress?
  ): List<Pair<Offset, Pair<EventAddress, Event>>> {
    val affected = getAffectedEvents(eventAddress, endAddress)
    return affected.mapNotNull { (key, event) ->
      if (key.eventAddress.barNum == 34) {
        ksLogv("bar 34")
      }
      addressToOffset(key.eventAddress)?.let { offsetInScore ->
        offsetInScore to (key.eventAddress to event)
      }
    }
  }

  private fun Score.createNewBars(
    eventAddress: EventAddress, timeSignature: TimeSignature,
    numBars: Int,
    formerNumBars: Int
  ): ScoreResult {
    return transformStaves(eventAddress.barNum, formerNumBars) { start, end, _ ->
      Right(removeSubLevels(start - 1, end - 1) as Stave)
    }.then {
      it.transformStaves(eventAddress.barNum, numBars) { start, end, _ ->
        val bars = (start..end).map {
          val ts = getTimeSignature(ez(it)) ?: timeSignature
          Bar(ts)
        }
        Right(addSubLevels(start, bars) as Stave)
      }
    }
  }

  private fun Score.readdEvents(offsetEvents: List<Pair<Offset, Pair<EventAddress, Event>>>): ScoreResult {
    return fold(offsetEvents.toList()) { (offset, pair) ->
      readdEvent(offset, pair.first, pair.second)
    }
  }

  private fun Score.removeOldEvents(offsetEvents: List<Pair<Offset, Pair<EventAddress, Event>>>): ScoreResult {
    val sorted = offsetEvents.sortedBy { it.first }
    ksLogv(sorted.toString())
    return fold(offsetEvents.toList()) { (_, pair) ->
      NewEventAdder.deleteEvent(this, pair.second.eventType, pair.second.params, pair.first)
    }
  }

  private fun Score.readdEvent(
    offset: Offset,
    oldAddress: EventAddress,
    event: Event
  ): ScoreResult {
    return offsetToAddress(offset).notNull().then { newAddress ->
      val fullAddress = oldAddress.copy(barNum = newAddress.barNum, offset = newAddress.offset)
      NewEventAdder.addEvent(
        this,
        event.eventType,
        event.params.plus(EventParam.CONSOLIDATE to true),
        fullAddress
      ).failureIsNoop(this) { it.balk(newAddress, event) }.ignoreFailure(this)
    }
  }

  private fun Score.balk(eventAddress: EventAddress, event: Event): ScoreResult {
    if (event.eventType == EventType.TUPLET) {
      getTimeSignature(eventAddress)?.let { timeSignature ->
        if (eventAddress.offset + event.realDuration() > timeSignature.duration) {
          return Left(Error("Not adding tuplet over bar"))
        }
      }
    }
    return Right(this)
  }

  private fun Score.getAffectedEvents(eventAddress: EventAddress,
  endAddress: EventAddress?): EventHash {
    return getAllEvents().filter { (key, event) ->
      !notThese.contains(key.eventType) && key.eventAddress.barNum >= eventAddress.barNum &&
          key.eventAddress.barNum < (endAddress?.barNum ?: (numBars + 1)) &&
          !isStartScoreEvent(key.eventAddress, event)
    }
  }

  private fun isStartScoreEvent(eventAddress: EventAddress, event: Event) =
    eventAddress.horizontal == Horizontal() && event.eventType == EventType.INSTRUMENT


  private val notThese = setOf(EventType.TIME_SIGNATURE, EventType.TIE)

  private fun Score.adjustLaterTimeSignatures(timeSignature: TimeSignature,
                                              oldScore:Score,
                                              eventAddress: EventAddress):ScoreResult {
    val allTimeSignatures = getEvents(EventType.TIME_SIGNATURE)?.toList() ?: listOf()
    val affected = allTimeSignatures.dropWhile { it.first.eventAddress.barNum <= eventAddress.barNum }
    return affected.firstOrNull()?.let { nextTs ->
      val distance = oldScore.getDuration(eventAddress, nextTs.first.eventAddress) ?: dZero()
      val barDiff = (distance / timeSignature.duration).toInt() - (nextTs.first.eventAddress.barNum - eventAddress.barNum)
      val newMap = allTimeSignatures.takeWhile { it.first.eventAddress.barNum <= eventAddress.barNum } +
          affected.map { it.first.copy(eventAddress = it.first.eventAddress + barDiff) to it.second }
      replaceSelf(eventMap.replaceEvents(EventType.TIME_SIGNATURE, newMap.toMap())).ok()
    } ?: this.ok()
  }

}
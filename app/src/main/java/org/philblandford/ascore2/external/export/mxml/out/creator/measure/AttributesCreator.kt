package com.philblandford.ascore.external.export.mxml.out.creator.measure

import com.philblandford.ascore.external.export.mxml.out.creator.RepeatBarQuery
import com.philblandford.kscore.api.instrument
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.duration.realDuration
import com.philblandford.kscore.engine.map.EventHash
import com.philblandford.kscore.engine.pitch.transposeKey
import com.philblandford.kscore.engine.time.timeSignature
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.util.lcm
import org.philblandford.ascore2.external.export.mxml.out.MxmlAttributes
import org.philblandford.ascore2.external.export.mxml.out.MxmlBeatType
import org.philblandford.ascore2.external.export.mxml.out.MxmlBeats
import org.philblandford.ascore2.external.export.mxml.out.MxmlChromatic
import org.philblandford.ascore2.external.export.mxml.out.MxmlClef
import org.philblandford.ascore2.external.export.mxml.out.MxmlDivisions
import org.philblandford.ascore2.external.export.mxml.out.MxmlFifths
import org.philblandford.ascore2.external.export.mxml.out.MxmlKey
import org.philblandford.ascore2.external.export.mxml.out.MxmlMeasureRepeat
import org.philblandford.ascore2.external.export.mxml.out.MxmlMeasureStyle
import org.philblandford.ascore2.external.export.mxml.out.MxmlStaffDetails
import org.philblandford.ascore2.external.export.mxml.out.MxmlStaffLines
import org.philblandford.ascore2.external.export.mxml.out.MxmlStaves
import org.philblandford.ascore2.external.export.mxml.out.MxmlTime
import org.philblandford.ascore2.external.export.mxml.out.MxmlTranspose
import org.philblandford.ascore2.external.export.mxml.out.clefToMxml
import kotlin.math.max


internal fun createAttributes(
  scoreQuery: ScoreQuery,
  eventAddress: EventAddress,
  staves: List<Int>,
  currentDivisions: Int,
  durationEvents: EventHash,
  repeatBarQuery: RepeatBarQuery
): MxmlAttributes? {

  var divisions = if (eventAddress.offset == dZero()) createDivisions(durationEvents) else null
  if (divisions?.num == currentDivisions && eventAddress.barNum > 1) divisions = null

  val key = createKey(scoreQuery, eventAddress)
  val time = createTime(scoreQuery, eventAddress)
  val stavesAttr = if (eventAddress.barNum == 1 && eventAddress.offset == dZero()) {
    MxmlStaves(staves.count())
  } else null
  val clefs = staves.mapNotNull {
    createClef(scoreQuery, eventAddress.copy(staveId = StaveId(eventAddress.staveId.main, it)))
  }
  val staffDetails = createStaffDetails(scoreQuery, eventAddress)
  val transpose = createTranspose(scoreQuery, eventAddress)
  val measureRepeats = createMeasureRepeats(repeatBarQuery, staves.count(), eventAddress)
  return if (!(divisions == null && key == null && time == null && stavesAttr == null && clefs.isEmpty() &&
        staffDetails == null && transpose == null && measureRepeats.count() == 0)
  ) {
    MxmlAttributes(
      divisions = divisions, key = key, time = time, staves = stavesAttr, clef = clefs,
      staffDetails = staffDetails, transpose = transpose, measureStyle = measureRepeats
    )
  } else {
    null
  }
}

private fun createKey(scoreQuery: ScoreQuery, eventAddress: EventAddress): MxmlKey? {
  return if (eventAddress.offset == dZero()) {

    scoreQuery.getEventAt(EventType.INSTRUMENT, eventAddress)?.let { (_, instrEvent) ->
      instrument(instrEvent)?.let { instrument ->
        if (instrument.percussion) {
          MxmlKey(MxmlFifths(0))
        } else {
          scoreQuery.getParam<Int>(
            EventType.KEY_SIGNATURE,
            EventParam.SHARPS,
            ez(eventAddress.barNum)
          )?.let {
            val key = transposeKey(it, -instrument.transposition)
            MxmlKey(MxmlFifths(key))
          }
        }
      }
    }
  } else null

}

private fun createTime(scoreQuery: ScoreQuery, eventAddress: EventAddress): MxmlTime? {
  return if (eventAddress.offset == dZero()) {
    getTimeSignature(eventAddress, scoreQuery)?.let { event ->
      timeSignature(event)?.let { ts ->
        MxmlTime(MxmlBeats(ts.numerator), MxmlBeatType(ts.denominator))
      }
    }
  } else null
}

private fun getTimeSignature(eventAddress: EventAddress, scoreQuery: ScoreQuery): Event? {
  return scoreQuery.getEvent(EventType.TIME_SIGNATURE, ez(eventAddress.barNum))?.let { event ->
    if (event.isTrue(EventParam.HIDDEN) && eventAddress.barNum == 1) {
      scoreQuery.getEvent(EventType.TIME_SIGNATURE, ez(eventAddress.barNum + 1))
    } else {
      event
    }
  }
}

private fun createDivisions(durationEvents: EventHash): MxmlDivisions? {
  val durations = durationEvents.map { it.value.realDuration() }
  val div = createDivisions(durations)
  return MxmlDivisions(div)
}

internal fun createDivisions(durations: List<Duration>): Int {
  return durations.fold(1) { divisions, duration ->
    val lcm = duration.denominator.lcm(divisions * 4)
    val div = crotchet().multiply(lcm).toInt()
    max(div, divisions)
  }
}

private fun createClef(scoreQuery: ScoreQuery, eventAddress: EventAddress): MxmlClef? {
  return scoreQuery.getParam<ClefType>(EventType.CLEF, EventParam.TYPE, eventAddress)?.let { clef ->
    clefToMxml(clef)?.let { mxmlClef ->
      if (scoreQuery.numStaves(eventAddress.staveId.main) > 1) {
        mxmlClef.copy(number = eventAddress.staveId.sub)
      } else {
        mxmlClef
      }
    }
  }
}

private fun createStaffDetails(
  scoreQuery: ScoreQuery,
  eventAddress: EventAddress
): MxmlStaffDetails? {
  return scoreQuery.getParamAt<Int>(EventType.INSTRUMENT, EventParam.STAVE_LINES, eventAddress)
    ?.let { lines ->
      MxmlStaffDetails(MxmlStaffLines(lines))
    }
}

private fun createTranspose(scoreQuery: ScoreQuery, eventAddress: EventAddress): MxmlTranspose? {
  return scoreQuery.getParam<Int>(EventType.INSTRUMENT, EventParam.TRANSPOSITION, eventAddress)
    ?.let { transposition ->
      if (transposition != 0) {
        MxmlTranspose(null, MxmlChromatic(transposition))
      } else {
        null
      }
    }
}

private fun createMeasureRepeats(
  repeatBarQuery: RepeatBarQuery,
  numStaves:Int,
  eventAddress: EventAddress
): List<MxmlMeasureStyle> {
  return if (eventAddress.offset == dZero()) {
    repeatBarQuery.getRepeatBars(eventAddress).map { repeatDesc ->
      val type = if (repeatDesc.start) "start" else "stop"
      val staff = if (numStaves == 1) null else eventAddress.staveId.sub
      MxmlMeasureStyle(staff, MxmlMeasureRepeat(type, repeatDesc.num))
    }
  } else listOf()
}
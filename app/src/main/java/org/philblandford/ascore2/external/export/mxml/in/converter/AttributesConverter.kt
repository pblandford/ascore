package com.philblandford.ascore.external.export.mxml.`in`.converter

import com.philblandford.ascore.external.export.mxml.out.MxmlAttributes
import com.philblandford.ascore.external.export.mxml.out.MxmlScorePart
import com.philblandford.ascore.external.export.mxml.out.creator.RepeatBarDesc
import com.philblandford.ascore.external.export.mxml.out.mxmlToClef
import com.philblandford.kscore.engine.pitch.KeySignature
import com.philblandford.kscore.engine.pitch.transposeKey
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.types.*

internal fun convertAttributes(
  mxmlAttributes: MxmlAttributes, measureState: MeasureState, mxmlScorePart: MxmlScorePart
): MeasureState {
  var stateCopy = measureState

  mxmlAttributes.clef.forEach { mxmlClef ->
    mxmlToClef(mxmlClef)?.let { clefType ->
      val staff = mxmlClef.number ?: 1
      val em = stateCopy.staveEvents.putEvent(
        ez(0, measureState.next.offset).copy(staveId = StaveId(0, staff)),
        Event(EventType.CLEF, paramMapOf(EventParam.TYPE to clefType))
      )
      val attributes =
        stateCopy.attributes.copy(clefs = stateCopy.attributes.clefs.plus(staff to clefType))
      stateCopy = stateCopy.copy(staveEvents = em, attributes = attributes)
    }
  }

  mxmlAttributes.time?.let { time ->
    val ts = TimeSignature(time.beat.num, time.beatType.num)
    val scoreEvents = stateCopy.scoreEvents.putEvent(eZero(), ts.toEvent())
    stateCopy = stateCopy.copy(
      attributes = stateCopy.attributes.copy(timeSignature = ts),
      scoreEvents = scoreEvents
    )
  }

  mxmlAttributes.key?.let { key ->
    if (!mxmlScorePart.isPercussion) {

      val transposition =
        mxmlAttributes.transpose?.chromatic?.num ?: measureState.attributes.transpose
      val sharps = transposeKey(key.fifths.num, transposition)

      val scoreEvents =
        stateCopy.scoreEvents.putEvent(eZero(), KeySignature(sharps).toEvent())
      stateCopy = stateCopy.copy(
        scoreEvents = scoreEvents,
        attributes = stateCopy.attributes.copy(keySignature = key.fifths.num)
      )
    }
  }

  mxmlAttributes.staves?.let { staves ->
    stateCopy = stateCopy.copy(attributes = stateCopy.attributes.copy(staves = staves.num))
  }

  mxmlAttributes.divisions?.let { divisions ->
    stateCopy = stateCopy.copy(attributes = stateCopy.attributes.copy(divisions = divisions.num))
  }

  mxmlAttributes.staffDetails?.staffLines?.let { lines ->
    stateCopy =
      stateCopy.copy(attributes = stateCopy.attributes.copy(staffLines = lines.num))
  }

  mxmlAttributes.transpose?.let { transpose ->
    stateCopy =
      stateCopy.copy(attributes = stateCopy.attributes.copy(transpose = transpose.chromatic.num))
  }

  mxmlAttributes.measureStyle.forEach { measureStyle ->
    measureStyle.measureRepeat?.let { mRepeat ->

      val active = mRepeat.type == "start"
      val staff = measureStyle.staff ?: 1
      val barNum = (measureState.attributes.repeatBars[staff]?.first ?: 0) + 1

      val map = stateCopy.attributes.repeatBars.plus(
        staff to Pair(
          barNum,
          RepeatBarDesc(active, mRepeat.slashes)
        )
      )
      stateCopy = stateCopy.copy(
        attributes = stateCopy.attributes.copy(repeatBars = map)
      )
    }
  }
  return stateCopy
}

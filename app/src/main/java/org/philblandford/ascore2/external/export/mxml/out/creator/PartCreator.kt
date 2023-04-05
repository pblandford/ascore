package com.philblandford.ascore.external.export.mxml.out.creator

import com.philblandford.ascore.external.export.mxml.out.MxmlPart
import com.philblandford.ascore.external.export.mxml.out.MxmlScorePart
import com.philblandford.ascore.external.export.mxml.out.creator.measure.createMeasure
import com.philblandford.kscore.engine.core.score.Part
import com.philblandford.kscore.engine.types.EventAddress
import com.philblandford.kscore.engine.types.ScoreQuery
import com.philblandford.kscore.engine.types.StaveId

internal fun createPart(
  part: Part, mxmlScorePart: MxmlScorePart,
  num: Int, scoreQuery: ScoreQuery, repeatBarQuery: RepeatBarQuery
): MxmlPart? {

  val eaToBars = part.staves.withIndex().flatMap {
    val staveId = StaveId(num, it.index + 1)
    it.value.bars.withIndex().map {
      EventAddress(it.index + 1, staveId = staveId) to it.value
    }
  }
  val groupedBars = eaToBars.groupBy { it.first.barNum }
    .map { it.key to it.value.map { it.first.staveId.sub to it.second } }

  val instrumentIdMap =
    mxmlScorePart.scoreInstrument.map { it.instrumentName.name to it.id }.toMap()
  val idLookup = { name: String ->
    if (mxmlScorePart.midiInstrument.count() > 1) {
      instrumentIdMap[name]
    } else {
      null
    }
  }

  var divisions = 1
  val measures = groupedBars.mapNotNull { (barNum, entry) ->
    val mxmlMeasure = createMeasure(
      entry.toMap(), num, barNum, divisions,
      idLookup,
      scoreQuery,
      repeatBarQuery
    )
    divisions = mxmlMeasure?.attributes()?.divisions?.num ?: divisions
    mxmlMeasure
  }

  return MxmlPart("P$num", measures)
}

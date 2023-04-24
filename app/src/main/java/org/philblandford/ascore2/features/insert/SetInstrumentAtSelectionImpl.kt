package org.philblandford.ascore2.features.insert

import com.philblandford.kscore.api.Instrument
import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.StaveId

class SetInstrumentAtSelectionImpl(private val kScore: KScore) : SetInstrumentAtSelection {

  override fun invoke(instrument: Instrument) {
    kScore.getSelectedArea()?.eventAddress?.let { address ->
      kScore.batch(
        { kScore.setInstrumentAtSelection(instrument) },
        *(instrument.clefs.withIndex().map { (idx, clef) ->
          {
            kScore.setParam(
              EventType.CLEF,
              EventParam.TYPE,
              clef,
              address.copy(staveId = StaveId(address.staveId.main, idx + 1))
            )
          }
        }).toTypedArray()
      )
    }
  }
}
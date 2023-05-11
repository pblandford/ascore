package com.philblandford.kscore.engine.eventadder.subadders

import com.philblandford.kscore.engine.core.score.Stave
import com.philblandford.kscore.engine.eventadder.BaseEventAdder
import com.philblandford.kscore.engine.types.EventAddress
import com.philblandford.kscore.engine.types.StaveId

internal object BeamSubAdder : LineSubAdderIf {


  override fun EventAddress.adjustForDestination(): EventAddress {
    return copy(staveId = StaveId(0, staveId.sub), id = 0)
  }
}






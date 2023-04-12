package com.philblandford.kscore.engine.newadder.duration

import com.philblandford.kscore.engine.newadder.NewSubAdder
import com.philblandford.kscore.engine.types.EventAddress
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.ParamMap
import com.philblandford.kscore.engine.types.isTrue

interface UpDownSubAdder : NewSubAdder {
  fun adjustAddress(eventAddress: EventAddress, params:ParamMap): EventAddress {
    return if (params.isTrue(EventParam.IS_UP)) {
      eventAddress
    } else {
      eventAddress.copy(id = 1)
    }
  }
}
package com.philblandford.kscore.engine.eventadder.subadders

import com.philblandford.kscore.engine.types.BowingType
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.ParamMap
import com.philblandford.kscore.engine.types.g

internal object BowingSubAdder : ChordDecorationSubAdder<BowingType> {
  override fun getParam(): EventParam {
    return EventParam.BOWING
  }

  override fun getParamVal(params:ParamMap): Any? {
    return params.g<BowingType>(EventParam.TYPE)
  }
}
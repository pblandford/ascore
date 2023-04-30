package com.philblandford.kscore.engine.eventadder.subadders

import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.ParamMap
import com.philblandford.kscore.engine.types.g

internal object FingeringSubAdder : ChordDecorationSubAdder<Int> {
  override fun getParam(): EventParam {
    return EventParam.FINGERING
  }

  override fun getParamVal(params:ParamMap): Any? {
    return params.g<Int>(EventParam.NUMBER)
  }

  override fun isUnique(): Boolean {
    return false
  }
}
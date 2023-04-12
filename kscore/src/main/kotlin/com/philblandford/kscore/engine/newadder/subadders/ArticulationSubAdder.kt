package com.philblandford.kscore.engine.newadder.subadders

import com.philblandford.kscore.engine.types.ArticulationType
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.ParamMap
import com.philblandford.kscore.engine.types.g

internal object ArticulationSubAdder : ChordDecorationSubAdder<ArticulationType> {
  override fun getParam(): EventParam {
    return EventParam.ARTICULATION
  }

  override fun getParamVal(params:ParamMap): Any? {
    return params.g<ArticulationType>(EventParam.TYPE)
  }

  override fun isUnique(): Boolean {
    return false
  }
}
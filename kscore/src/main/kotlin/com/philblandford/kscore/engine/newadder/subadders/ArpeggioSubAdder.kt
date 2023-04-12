package com.philblandford.kscore.engine.newadder.subadders

import com.philblandford.kscore.engine.types.*

object ArpeggioSubAdder : ChordDecorationSubAdder<ArticulationType> {
  override fun getParam(): EventParam {
    return EventParam.ARPEGGIO
  }

  override fun getParamVal(params:ParamMap): Any? {
    return params.g<ArpeggioType>(EventParam.TYPE) ?: ArpeggioType.NORMAL
  }

  override fun isUnique(): Boolean {
    return false
  }
}
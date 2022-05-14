package org.philblandford.ui.insert.model

import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.ParamMap
import org.philblandford.ui.base.viewmodel.VMModel

open class InsertModel(
  open val title:Int,
  open val helpTag:String,
  open val params:ParamMap) : VMModel() {
  fun <T>getParam(key:EventParam):T = params[key] as T
}


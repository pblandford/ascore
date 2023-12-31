package org.philblandford.ascore2.features.ui.usecases

import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.ParamMap
import org.philblandford.ascore2.features.ui.model.InsertItem

interface UpdateInsertParams {
  operator fun invoke(paramFunc:ParamMap.()->ParamMap)
}
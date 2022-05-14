package org.philblandford.ui.insert.items.tuplet.model

import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.paramMapOf
import org.philblandford.ui.insert.items.tuplet.viewmodel.TupletInsertModel



val tupletModel = TupletInsertModel(
  2, 32,
  paramMapOf(
    EventParam.NUMERATOR to 3,
    EventParam.HIDDEN to false
  )
)
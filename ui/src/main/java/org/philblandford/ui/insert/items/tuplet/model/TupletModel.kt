package org.philblandford.ui.insert.items.tuplet.model

import com.philblandford.kscore.engine.types.ParamMap
import com.philblandford.kscore.engine.types.paramMapOf
import org.philblandford.ui.insert.model.InsertModel

data class TupletInsertModel(
  val minNumerator: Int = 2,
  val maxNumerator: Int = 32,
) : InsertModel()



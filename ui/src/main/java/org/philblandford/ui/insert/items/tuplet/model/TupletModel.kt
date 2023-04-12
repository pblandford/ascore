package org.philblandford.ui.insert.items.tuplet.model

import org.philblandford.ui.insert.model.InsertModel

data class TupletInsertModel(
  val minNumerator: Int = 2,
  val maxNumerator: Int = 32,
) : InsertModel()



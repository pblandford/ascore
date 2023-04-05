package org.philblandford.ui.insert.items.lyric.model

import org.philblandford.ui.insert.model.InsertModel


data class LyricInsertModel(
  val number: Int = 1,
  val maxNum: Int = 3,
) : InsertModel()


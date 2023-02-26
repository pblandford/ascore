package org.philblandford.ui.insert.items.lyric.model

import com.philblandford.kscore.engine.types.ParamMap
import org.philblandford.ascore2.features.ui.model.InsertItem
import org.philblandford.ascore2.features.ui.model.stubItem
import org.philblandford.ui.insert.model.InsertModel


data class LyricInsertModel(
  val maxNum: Int = 3,
) : InsertModel()


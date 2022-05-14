package org.philblandford.ui.insert.items.articulation.model

import com.philblandford.kscore.engine.types.ArticulationType
import org.philblandford.ui.R
import org.philblandford.ui.insert.row.viewmodel.RowInsertModel

private val articulationIds = listOf(
  R.drawable.accent to ArticulationType.ACCENT,
  R.drawable.staccato_icon to ArticulationType.STACCATO,
  R.drawable.tenuto_icon to ArticulationType.TENUTO,
  R.drawable.marcato_icon to ArticulationType.MARCATO,
  R.drawable.spiccato_icon to ArticulationType.STACCATISSIMO
)

val articulationModel = RowInsertModel(
  R.string.articulation, "",
  articulationIds, 0
)
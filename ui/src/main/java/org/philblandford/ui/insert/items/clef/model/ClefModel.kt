package org.philblandford.ui.insert.items.clef.model

import com.philblandford.kscore.engine.types.ClefType
import org.philblandford.ui.R
import org.philblandford.ui.insert.row.viewmodel.RowInsertModel

private val clefIds = listOf(
  R.drawable.treble_clef to ClefType.TREBLE,
  R.drawable.bass_clef to ClefType.BASS,
  R.drawable.alto_clef to ClefType.ALTO,
  R.drawable.tenor_clef to ClefType.TENOR,
  R.drawable.mezzo_clef to ClefType.MEZZO,
  R.drawable.soprano_clef to ClefType.SOPRANO,
  R.drawable.treble_octava_up to ClefType.TREBLE_8VA,
  R.drawable.treble_octava_down to ClefType.TREBLE_8VB,
  R.drawable.bass_clef_octava_up to ClefType.BASS_8VA,
  R.drawable.bass_clef_octava_down to ClefType.BASS_8VB
)

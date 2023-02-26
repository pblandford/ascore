package org.philblandford.ui.insert.items.ornament.model

import com.philblandford.kscore.engine.types.*
import org.philblandford.ui.R
import org.philblandford.ui.insert.row.viewmodel.RowInsertModel

private val ornamentIds = listOf(
  R.drawable.trill to OrnamentType.TRILL,
  R.drawable.trill_part to EventType.LONG_TRILL,
  R.drawable.turn to OrnamentType.TURN,
  R.drawable.mordent to OrnamentType.MORDENT,
  R.drawable.lower_mordent to OrnamentType.LOWER_MORDENT,
  R.drawable.arpeggio to EventType.ARPEGGIO
)

typealias OrnamentInsertModel = RowInsertModel<Enum<*>>
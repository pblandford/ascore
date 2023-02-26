package org.philblandford.ui.insert.items.keysignature.compose

import androidx.compose.runtime.Composable
import com.philblandford.kscore.engine.types.EventParam
import org.philblandford.ui.insert.row.compose.RowInsert
import org.philblandford.ui.util.keySignatureIds

@Composable
fun KeySignatureInsert() {
  RowInsert(keySignatureIds, 0, EventParam.SHARPS, 2)
}
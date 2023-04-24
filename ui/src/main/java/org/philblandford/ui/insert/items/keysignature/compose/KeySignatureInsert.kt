package org.philblandford.ui.insert.items.keysignature.compose

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import org.philblandford.ui.insert.row.compose.RowInsert
import org.philblandford.ui.main.window.LocalWindowSizeClass
import org.philblandford.ui.main.window.compact
import org.philblandford.ui.util.keySignatureIds

@Composable
fun KeySignatureInsert() {
  RowInsert(keySignatureIds, 0, if (LocalWindowSizeClass.current.compact()) 3 else 1)
}
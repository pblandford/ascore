package org.philblandford.ui.keyboard.compose.selectors

import androidx.compose.runtime.Composable
import com.philblandford.kscore.engine.types.NoteHeadType
import org.philblandford.ui.util.ImageGridDropdown
import org.philblandford.ui.util.noteHeadIds

@Composable
fun NoteHeadSelector(current: NoteHeadType, onSelect: (NoteHeadType) -> Unit) {
  ImageGridDropdown(
    images = noteHeadIds.map { it.first },
    columns = 1, rows = noteHeadIds.size,
    selected = {
      noteHeadIds.indexOfFirst { it.second == current }
    }, tag = { "NoteHead ${noteHeadIds[it].second}" },
    onSelect = { onSelect(noteHeadIds[it].second) }
  )
}
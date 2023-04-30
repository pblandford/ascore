package org.philblandford.ui.clipboard.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.philblandford.kscore.engine.types.NoteHeadType
import org.philblandford.ui.R
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.clipboard.viewmodel.ClipboardInterface
import org.philblandford.ui.clipboard.viewmodel.ClipboardViewModel
import org.philblandford.ui.input.compose.selectors.NoteHeadSelector
import org.philblandford.ui.theme.compose.AscoreTheme

@Composable
fun ClipboardExtraView() {
  VMView(ClipboardViewModel::class.java) { model, iface, _ ->
    AscoreTheme(
      MaterialTheme.colorScheme.copy(
        surface = MaterialTheme.colorScheme.onSurface,
        onSurface = MaterialTheme.colorScheme.surface
      )
    ) {
      ClipboardExtraViewInternal(iface)
    }
  }
}

@Composable
private fun ClipboardExtraViewInternal(iface: ClipboardInterface) {
  Row(
    Modifier
      .border(1.dp, Color.Black)
      .background(MaterialTheme.colorScheme.surface)
      .padding(2.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    NoteHeadSelector(NoteHeadType.NORMAL) { iface.setNoteHead(it) }
    Item(R.drawable.small) {
      iface.toggleSmall()
    }
    Item(R.drawable.up, { iface.noteUp(true) }) { iface.noteUp(false) }
    Item(R.drawable.down, { iface.noteDown(true) }) { iface.noteDown(false) }
    Item(R.drawable.tie) { iface.addTies() }
    Item(R.drawable.crotchet_up, { iface.removeStems() }) { iface.setStems() }
  }
}
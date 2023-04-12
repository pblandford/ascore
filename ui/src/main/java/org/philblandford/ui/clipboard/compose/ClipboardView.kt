package org.philblandford.ui.clipboard.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.philblandford.ui.R
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.clipboard.viewmodel.ClipboardInterface
import org.philblandford.ui.clipboard.viewmodel.ClipboardViewModel
import org.philblandford.ui.common.block
import org.philblandford.ui.util.SquareButton
import timber.log.Timber

@Composable
fun ClipboardView(modifier:Modifier) {
  Timber.e("X")
  VMView(ClipboardViewModel::class.java, modifier) { _, iface, _->
    Timber.e("Y")
    ClipboardViewInternal(modifier, iface)
  }
}

@Composable
fun ClipboardViewInternal(modifier: Modifier, iface:ClipboardInterface) {
  Row(modifier.border(1.dp, Color.Black).background(MaterialTheme.colors.onSurface).padding(2.dp)) {
    Item(R.drawable.left_arrow) { iface.selectionLeft()  }
    Item(R.drawable.right_arrow) { iface.selectionRight()  }
    Item(R.drawable.copy) { iface.copy() }
    Item(R.drawable.cut) { iface.cut() }
    Item(R.drawable.paste) { iface.paste() }
  }
}

@Composable
private fun Item(id: Int,  cmd: () -> Unit) {
  SquareButton(id, size = block(),
    foregroundColor = MaterialTheme.colors.surface, backgroundColor = MaterialTheme.colors.onSurface) { cmd() }
}
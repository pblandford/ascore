package org.philblandford.ui.insert.items.timesignature.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.types.EventParam
import org.philblandford.ascore2.features.ui.model.InsertItem
import org.philblandford.ui.R
import org.philblandford.ui.common.block
import org.philblandford.ui.insert.common.compose.InsertVMView
import org.philblandford.ui.insert.common.viewmodel.DefaultInsertViewModel
import org.philblandford.ui.insert.model.InsertInterface
import org.philblandford.ui.insert.model.InsertModel
import org.philblandford.ui.util.ButtonState.Companion.dimmed
import org.philblandford.ui.util.Gap
import org.philblandford.ui.util.SquareButton
import org.philblandford.ui.util.TimeSignatureSelector

@Composable
fun TimeSignatureInsert() {
  InsertVMView<InsertModel, InsertInterface<InsertModel>, DefaultInsertViewModel> { _, insertItem, iface ->
    TimeSignatureInsertInternal(insertItem, iface)
  }
}

@Composable
private fun TimeSignatureInsertInternal(
  insertItem: InsertItem,
  iface: InsertInterface<InsertModel>
) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    TimeSignatureSelector(
      TimeSignature.fromParams(insertItem.params),
      { iface.updateParams(it.toEvent().params) })

    val hidden = insertItem.getParam<Boolean>(EventParam.HIDDEN) ?: false
    Gap(0.5f)
    SquareButton(R.drawable.hidden, Modifier.size(block()), dimmed(hidden)) {
      iface.setParam(EventParam.HIDDEN, !hidden)
    }

  }
}
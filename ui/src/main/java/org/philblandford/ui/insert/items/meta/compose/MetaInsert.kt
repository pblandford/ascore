package org.philblandford.ui.insert.items.meta.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.MetaType
import org.philblandford.ascore2.features.ui.model.InsertItem
import org.philblandford.ui.R
import org.philblandford.ui.common.Gap
import org.philblandford.ui.insert.common.compose.InsertVMView
import org.philblandford.ui.insert.items.meta.model.MetaInsertModel
import org.philblandford.ui.insert.items.meta.viewmodel.MetaInsertInterface
import org.philblandford.ui.insert.items.meta.viewmodel.MetaInsertCommonViewModel
import org.philblandford.ui.insert.items.meta.viewmodel.MetaInsertViewModel
import org.philblandford.ui.util.TextSpinner
import org.philblandford.ui.util.resourceId

@Composable
fun MetaInsert() {
  InsertVMView<MetaInsertModel, MetaInsertInterface, MetaInsertViewModel> { model, insertItem, iface ->
    MetaInsertInternal(model, insertItem, iface) { insertItem ->
        Selection(insertItem, iface)
    }
  }
}


@Composable
private fun Selection(insertItem: InsertItem, insertInterface: MetaInsertInterface) {
  val strings =
    linkedMapOf(*MetaType.entries.map { it.toEventType() to stringResource(it.resourceId()) }
      .toTypedArray())
  Row(verticalAlignment = Alignment.CenterVertically) {
    Text("${stringResource(R.string.type)}:", fontSize = 15.sp)
    Gap(0.5f)
    TextSpinner(strings.values.toList(), selected = {
      strings[insertItem.eventType] ?: "Title"
    }, onSelect = { selected ->
      insertInterface.setEventType(
        strings.toList().withIndex().find { it.index == selected }?.value?.first ?: EventType.TITLE
      )
    })
  }
}


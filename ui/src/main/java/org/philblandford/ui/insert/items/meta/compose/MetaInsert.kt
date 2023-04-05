package org.philblandford.ui.insert.items.meta.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.MetaType
import org.philblandford.ascore2.features.ui.model.InsertItem
import org.philblandford.ui.common.Gap
import org.philblandford.ui.common.block
import org.philblandford.ui.insert.common.compose.InsertVMView
import org.philblandford.ui.insert.items.meta.viewmodel.MetaInsertInterface
import org.philblandford.ui.insert.items.meta.viewmodel.MetaInsertViewModel
import org.philblandford.ui.insert.model.InsertInterface
import org.philblandford.ui.insert.model.InsertModel
import org.philblandford.ui.util.TextSpinner
import org.philblandford.ui.util.resourceId
import timber.log.Timber

@Composable
fun MetaInsert() {

  InsertVMView<InsertModel, MetaInsertInterface, MetaInsertViewModel> { _, insertItem, iface ->
    MetaInsertInternal(insertItem, iface)
  }
}

@Composable
private fun MetaInsertInternal(insertItem: InsertItem, iface: MetaInsertInterface) {

  val text = remember(insertItem.eventType to insertItem.params) { mutableStateOf(insertItem.getParam<String>(EventParam.TEXT) ?: "") }

  Row(
    Modifier
      .fillMaxWidth()
      .padding(10.dp)) {
    Box(
      Modifier
        .width(block(4.5f))
        .align(Alignment.CenterVertically)) {

      TextField(
        modifier = Modifier.wrapContentHeight().width(block(5)).background(MaterialTheme.colors.primaryVariant),
        value = text.value,
        onValueChange = {
          text.value = it
          iface.insertText(it)
        }
      )
    }
    Gap(0.5f)
    Box(Modifier.align(Alignment.CenterVertically)) {
      Selection(insertItem, iface)
    }
  }
}

@Composable
private fun Selection(insertItem: InsertItem, insertInterface: InsertInterface<InsertModel>) {
  val strings =
    linkedMapOf(*MetaType.values().map { it.toEventType() to stringResource(it.resourceId()) }
      .toTypedArray())
  TextSpinner(strings.values.toList(), selected = {
    strings[insertItem.eventType] ?: "Title"
  }, onSelect = { selected ->
    insertInterface.setEventType(strings.toList().withIndex().find { it.index == selected }?.value?.first ?: EventType.TITLE)
  })
}

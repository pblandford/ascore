package org.philblandford.ui.insert.items.meta.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.MetaType
import org.philblandford.ascore2.features.ui.model.InsertItem
import org.philblandford.ui.R
import org.philblandford.ui.common.Gap
import org.philblandford.ui.common.block
import org.philblandford.ui.insert.common.compose.InsertVMView
import org.philblandford.ui.insert.items.meta.model.MetaInsertModel
import org.philblandford.ui.insert.items.meta.viewmodel.MetaInsertInterface
import org.philblandford.ui.insert.items.meta.viewmodel.MetaInsertViewModel
import org.philblandford.ui.util.TextSpinner
import org.philblandford.ui.util.nullIfEmpty
import org.philblandford.ui.util.resourceId

@Composable
fun MetaInsert() {

  InsertVMView<MetaInsertModel, MetaInsertInterface, MetaInsertViewModel> { model, insertItem, iface ->
    MetaInsertInternal(model, insertItem, iface)
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MetaInsertInternal(
  model: MetaInsertModel,
  insertItem: InsertItem,
  iface: MetaInsertInterface
) {

  val text = remember(insertItem.eventType to insertItem.params) {
    mutableStateOf(
      insertItem.getParam<String>(EventParam.TEXT) ?: ""
    )
  }

  Column {
    Selection(insertItem, iface)
    Row(
      Modifier
        .fillMaxWidth()
        .padding(10.dp), verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        Modifier
          .width(block(4.5f))
          .align(Alignment.CenterVertically)
      ) {

        TextField(
          modifier = Modifier
            .wrapContentHeight()
            .width(block(5))
            .background(MaterialTheme.colorScheme.onSurface),
          value = text.value,
          colors = TextFieldDefaults.textFieldColors(textColor = MaterialTheme.colorScheme.surface,
            containerColor = MaterialTheme.colorScheme.onSurface,
            cursorColor = MaterialTheme.colorScheme.surface
            ),
          onValueChange = {
            text.value = it
            iface.insertText(it)
          }
        )
      }
      Gap(0.5f)
      FontSelection(model.fonts, insertItem, iface)
    }
  }
}

@Composable
private fun Selection(insertItem: InsertItem, insertInterface: MetaInsertInterface) {
  val strings =
    linkedMapOf(*MetaType.values().map { it.toEventType() to stringResource(it.resourceId()) }
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

@Composable
private fun FontSelection(fonts: List<String>, insertItem: InsertItem, iface: MetaInsertInterface) {
  Column {

    Text("${stringResource(R.string.font)}:", fontSize = 15.sp)
    TextSpinner(fonts, selected = {
      insertItem.getParam<String>(EventParam.FONT)?.nullIfEmpty() ?: "default"
    }, onSelect = { selected ->
      iface.setParam(EventParam.FONT, fonts.withIndex().find { it.index == selected }?.value ?: "")
    })
  }
}
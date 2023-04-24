package org.philblandford.ui.insert.items.text.compose

import GridSelection
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import org.philblandford.ascore2.features.ui.model.InsertItem
import org.philblandford.ui.R
import org.philblandford.ui.common.block
import org.philblandford.ui.insert.common.compose.InsertVMView
import org.philblandford.ui.insert.common.viewmodel.DefaultInsertViewModel
import org.philblandford.ui.insert.items.text.viewmodel.TextInsertViewModel
import org.philblandford.ui.insert.model.InsertInterface
import org.philblandford.ui.insert.model.InsertModel
import org.philblandford.ui.main.window.LocalWindowSizeClass
import org.philblandford.ui.main.window.compact
import org.philblandford.ui.util.Gap
import org.philblandford.ui.util.ToggleRow

@Composable
fun TextInsert() {
  InsertVMView<InsertModel, InsertInterface<InsertModel>, TextInsertViewModel> { _, insertItem, iface ->
    TextInsertInternal(insertItem, iface)
  }
}


@Composable
private fun TextInsertInternal(
  insertItem: InsertItem,
  iface: InsertInterface<InsertModel>
) {
  if (LocalWindowSizeClass.current.compact()) {
    TextInsertCompact(insertItem, iface)
  } else {
    TextInsertExpanded(insertItem, iface)
  }
}

@Composable
private fun TextInsertCompact(
  insertItem: InsertItem,
  iface: InsertInterface<InsertModel>
) {
  Column(Modifier.padding(10.dp)) {
    Insert(insertItem, iface)
    Gap(0.5f)
    Row {
      Select(insertItem, iface)
      if (insertItem.eventType == EventType.EXPRESSION_TEXT) {
        Gap(block())
        UpDown(insertItem, iface)
      }
    }
  }
}

@Composable
private fun TextInsertExpanded(
  insertItem: InsertItem,
  iface: InsertInterface<InsertModel>
) {
  Row(Modifier.padding(10.dp)) {
    Insert(insertItem, iface)
    Gap(0.5f)
      Select(insertItem, iface)
      if (insertItem.eventType == EventType.EXPRESSION_TEXT) {
        Gap(block())
        UpDown(insertItem, iface)
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Insert(insertItem: InsertItem, iface: InsertInterface<InsertModel>) {
  val text =
    remember(insertItem.eventType) { mutableStateOf(insertItem.getParam(EventParam.TEXT) ?: "") }

  OutlinedTextField(
    value = text.value,
    onValueChange = {
      text.value = it
      iface.setParam(EventParam.TEXT, it)
    },
    modifier = Modifier
      .size(block(7), block(2))
      .testTag("MainText"),
    colors = TextFieldDefaults.textFieldColors(
      textColor = MaterialTheme.colorScheme.surface,
      containerColor = MaterialTheme.colorScheme.onSurface,
      cursorColor = MaterialTheme.colorScheme.surface
    )
  )
}

@Composable
private fun Select(insertItem: InsertItem, iface: InsertInterface<InsertModel>) {
  GridSelection(
    images = ids.map { it.first },
    selected = { ids.indexOfFirst { it.second == insertItem.eventType } },
    onSelect = {
      iface.setEventType(ids[it].second)
    },
    tag = { ids[it].second.toString() }, rows = 1, columns = ids.size
  )
}

@Composable
private fun UpDown(insertItem: InsertItem, iface: InsertInterface<InsertModel>) {
  Box(Modifier.testTag("UpDown")) {
    ToggleRow(ids = listOf(R.drawable.up, R.drawable.down),
      selected =
      if (insertItem.getParam<Boolean?>(EventParam.IS_UP) == true) 0 else 1,
      onSelect = { iface.setParam(EventParam.IS_UP, it == 0) },
      tag = { it.toString() })
  }
}


private val ids = listOf(
  R.drawable.text to EventType.TEMPO_TEXT,
  R.drawable.italic to EventType.EXPRESSION_TEXT,
  R.drawable.rehearsal_mark to EventType.REHEARSAL_MARK
)
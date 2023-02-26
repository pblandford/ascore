package org.philblandford.ui.insert.items.tie.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.philblandford.ui.insert.common.compose.InsertVMView
import org.philblandford.ui.insert.common.viewmodel.DefaultInsertViewModel
import org.philblandford.ui.insert.model.InsertInterface
import org.philblandford.ui.insert.model.InsertModel

@Composable
fun TieInsert() {
  InsertVMView<InsertModel, InsertInterface<InsertModel>,
          DefaultInsertViewModel> { _, _, _ ->
    Box(
      Modifier
        .fillMaxWidth()
        .height(60.dp))
  }
}
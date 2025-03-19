package org.philblandford.ui.insert.items.meta.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.philblandford.kscore.engine.types.EventType
import org.philblandford.ascore2.features.ui.model.InsertItem
import org.philblandford.ui.R
import org.philblandford.ui.insert.common.compose.InsertVMView
import org.philblandford.ui.insert.items.meta.model.MetaInsertModel
import org.philblandford.ui.insert.items.meta.viewmodel.FooterInsertViewModel
import org.philblandford.ui.insert.items.meta.viewmodel.MetaInsertInterface
import org.philblandford.ui.insert.items.meta.viewmodel.MetaInsertCommonViewModel

@Composable
fun FooterInsert() {
    InsertVMView<MetaInsertModel, MetaInsertInterface, FooterInsertViewModel> { model, insertItem, iface ->
        MetaInsertInternal(model, insertItem, iface) {
            Selection(insertItem, iface)
        }
    }
}

@Composable
private fun Selection(insertItem: InsertItem, insertInterface: MetaInsertInterface) {

    Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(
            selected = insertItem.eventType == EventType.FOOTER_LEFT,
            onClick = { insertInterface.setEventType(EventType.FOOTER_LEFT) }
        )
        Text("${stringResource(R.string.left)}:", fontSize = 15.sp)
        RadioButton(
            selected = insertItem.eventType == EventType.FOOTER_CENTER,
            onClick = { insertInterface.setEventType(EventType.FOOTER_CENTER) }
        )
        Text("${stringResource(R.string.centre)}:", fontSize = 15.sp)
        RadioButton(
            selected = insertItem.eventType == EventType.FOOTER_RIGHT,
            onClick = { insertInterface.setEventType(EventType.FOOTER_RIGHT) }
        )
        Text("${stringResource(R.string.right)}:", fontSize = 15.sp)
    }
}

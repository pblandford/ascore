package org.philblandford.ui.insert.items.meta.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import org.philblandford.ascore2.features.ui.model.InsertItem
import org.philblandford.ui.R
import org.philblandford.ui.common.Gap
import org.philblandford.ui.common.block
import org.philblandford.ui.insert.items.meta.model.MetaInsertModel
import org.philblandford.ui.insert.items.meta.viewmodel.MetaInsertInterface
import org.philblandford.ui.util.TextSpinner
import org.philblandford.ui.util.nullIfEmpty


@Composable
internal fun MetaInsertInternal(
    model: MetaInsertModel,
    insertItem: InsertItem,
    iface: MetaInsertInterface,
    selection: @Composable (InsertItem) -> Unit
) {

    val text = remember(insertItem.eventType to insertItem.params) {
        mutableStateOf(
            insertItem.getParam<String>(EventParam.TEXT) ?: ""
        )
    }

    Column {
        selection(insertItem)
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
                    colors = TextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.surface,
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
internal fun FontSelection(fonts: List<String>, insertItem: InsertItem, iface: MetaInsertInterface) {
    Column {

        Text("${stringResource(R.string.font)}:", fontSize = 15.sp)
        TextSpinner(fonts, selected = {
            insertItem.getParam<String>(EventParam.FONT)?.nullIfEmpty() ?: "default"
        }, onSelect = { selected ->
            iface.setParam(EventParam.FONT, fonts.withIndex().find { it.index == selected }?.value ?: "")
        })
    }
}
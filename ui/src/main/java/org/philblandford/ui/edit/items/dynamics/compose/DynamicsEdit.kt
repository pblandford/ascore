package org.philblandford.ui.edit.items.dynamics.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import com.philblandford.kscore.engine.types.EventParam
import org.philblandford.ui.R
import org.philblandford.ui.edit.compose.RowEdit
import org.philblandford.ui.edit.entities.ButtonActions
import org.philblandford.ui.util.*
import org.philblandford.ui.util.dynamicIds

@Composable
fun DynamicsEdit(scale:Float) {
    RowEdit(dynamicIds, scale, 3) { model, iface ->
        Box(Modifier.fillMaxHeight()) {
            UpDownColumn(model.editItem.event.getParam<Boolean>(EventParam.IS_UP) ?: false, {
                iface.updateParam(EventParam.IS_UP, it)
            }, Modifier.align(Alignment.Center))
        }
    }
}
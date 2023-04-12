package org.philblandford.ui.edit.items.dynamics.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.philblandford.kscore.engine.types.EventParam
import org.philblandford.ui.R
import org.philblandford.ui.edit.compose.RowEdit
import org.philblandford.ui.edit.entities.ButtonActions
import org.philblandford.ui.util.*
import org.philblandford.ui.util.dynamicIds

@Composable
fun DynamicsEdit(scale:Float) {
    RowEdit(dynamicIds, scale, 3) { model, iface ->
        UpDownRow(model.editItem.event.getParam<Boolean>(EventParam.IS_UP) ?: false, {
            iface.updateParam(EventParam.IS_UP, it)
        })
    }
}
package org.philblandford.ui.insert.items.transposeto.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.log.ksLogt
import org.philblandford.ascore2.features.ui.model.InsertItem
import org.philblandford.ui.R
import org.philblandford.ui.insert.common.compose.DefaultInsertVMView
import org.philblandford.ui.insert.common.compose.InsertVMView
import org.philblandford.ui.insert.items.transposeby.viewmodel.TransposeInterface
import org.philblandford.ui.insert.items.transposeby.viewmodel.TransposeViewModel
import org.philblandford.ui.insert.model.DefaultInsertInterface
import org.philblandford.ui.insert.model.InsertModel
import org.philblandford.ui.util.Gap
import org.philblandford.ui.util.KeySelector
import org.philblandford.ui.util.SquareButton
import org.philblandford.ui.util.UpDownDependent

@Composable
fun TransposeTo() {
  InsertVMView<InsertModel, TransposeInterface, TransposeViewModel>() { _, item, iface ->
    TransposeToInternal(item, iface)
  }
}

@Composable
private fun TransposeToInternal(insertItem: InsertItem, iface:TransposeInterface) {
  Row(Modifier.fillMaxWidth()) {
    KeySelector(selected =  insertItem.getParam(EventParam.SHARPS) ?: 0 ,
      onSelect = {
        ksLogt("key $it")
        iface.setParam(EventParam.SHARPS, it)
      }, rows = 3, modifier = Modifier.align(Alignment.CenterVertically))
    Gap(0.5f)
    UpDownDependent(
      Modifier.align(Alignment.CenterVertically),
      { insertItem.getParam<Boolean>(EventParam.IS_UP) == true },
      { iface.setParam(EventParam.IS_UP, it) })
    Gap(0.5f)
    SquareButton(
      R.drawable.tick, modifier = Modifier.align(Alignment.CenterVertically)) {
      iface.go()
    }
  }
}
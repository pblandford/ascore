package org.philblandford.ui.insert.items.lyric.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.g
import com.philblandford.kscore.log.ksLogt
import org.philblandford.ascore2.features.ui.model.InsertItem
import org.philblandford.ui.R
import org.philblandford.ui.insert.common.compose.InsertVMView
import org.philblandford.ui.insert.items.lyric.model.LyricInsertModel
import org.philblandford.ui.insert.items.lyric.viewmodel.LyricInsertInterface
import org.philblandford.ui.insert.items.lyric.viewmodel.LyricInsertViewModel
import org.philblandford.ui.util.FreeKeyboard
import org.philblandford.ui.util.Gap
import org.philblandford.ui.util.NumberPicker
import org.philblandford.ui.util.SquareButton
import timber.log.Timber

@Composable
fun LyricInsert() {
  InsertVMView<LyricInsertModel,
          LyricInsertInterface,
          LyricInsertViewModel> { model, insertItem, iface ->
    LyricInsertInternal(model, insertItem, iface)
  }
}


@Composable
fun LyricInsertInternal(model: LyricInsertModel,insertItem: InsertItem, iface:LyricInsertInterface) {
  val text = insertItem.getParam<String?>(EventParam.TEXT) ?: ""

  FreeKeyboard(
    initValue = text,
    tag = "LyricTextField",
    onValueChanged = {
      iface.insertLyric(it)
    }) {
    Row {
      Box {
        NumberPicker(min = 1, max = model.maxNum,
          getNum = { insertItem.getParam(EventParam.NUMBER) }, setNum = {
            iface.setParam(EventParam.NUMBER, it)
          }, editable = false
        )
      }

      LeftRight(iface, Modifier.align(Alignment.CenterVertically))
      Gap(0.5f)
      SquareButton(resource = R.drawable.keyboard_letter, onClick = {
        show()
      })
    }
  }

}

@Composable
private fun LeftRight(
  iface: LyricInsertInterface,
  modifier: Modifier
) {
  Row(modifier) {
    SquareButton(R.drawable.left_arrow, tag = "Left", border = true) {
      iface.markerLeft()
    }
    SquareButton(R.drawable.right_arrow, tag = "Right", border = true) {
      iface.markerRight()
    }
  }
}

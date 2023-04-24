package org.philblandford.ui.insert.items.pagemargins.compose

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.philblandford.kscore.engine.types.EventParam
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.insert.common.compose.InsertVMView
import org.philblandford.ui.insert.items.pagemargins.model.PageMarginsModel
import org.philblandford.ui.insert.items.pagemargins.viewmodel.PageMarginsInterface
import org.philblandford.ui.insert.items.pagemargins.viewmodel.PageMarginsViewModel
import org.philblandford.ui.util.Gap
import org.philblandford.ui.util.SquareButton
import org.philblandford.ui.R
import org.philblandford.ui.common.block
import org.philblandford.ui.insert.items.pagemargins.model.MarginDescriptor
import org.philblandford.ui.util.NumberSelector

@Composable
fun PageMargins() {
  InsertVMView<PageMarginsModel, PageMarginsInterface, PageMarginsViewModel> { model, _, iface ->
    PageMarginsInternal(model, iface)
  }
}

@Composable
private fun PageMarginsInternal(model: PageMarginsModel, iface: PageMarginsInterface) {

  Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.Bottom) {
    Column {
      MarginRow(model.left, model, iface)
      MarginRow(model.right, model, iface)
      MarginRow(model.top, model, iface)
      MarginRow(model.bottom, model, iface)
    }
    Gap(0.5f)
    SquareButton(R.drawable.cross, size = 25.dp) { iface.clear() }
  }
}


@Composable
private fun MarginRow(
  marginDescriptor: MarginDescriptor,
  model: PageMarginsModel, iface: PageMarginsInterface,
  abbreviate:Boolean = false
) {
  var text = stringResource(id = marginDescriptor.textId)
  if (abbreviate) {
    text = text.take(1)
  }
  val width = if (abbreviate) 1f else 2.5f
  Row {
    Text(text, androidx.compose.ui.Modifier.width(block(width)))
    Gap(block(0.5f))
    Box(Modifier.border(1.dp, MaterialTheme.colorScheme.onSurface)) {
      NumberSelector(min = model.min, max = model.max,
        num = marginDescriptor.current, setNum = {
          iface.setMargin(marginDescriptor.param, it)
        }, step = model.step, tag = { text })
    }
  }
}


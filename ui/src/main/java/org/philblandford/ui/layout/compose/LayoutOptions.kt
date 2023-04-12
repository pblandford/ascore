package org.philblandford.ui.layout.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.log.ksLogt
import org.philblandford.ui.R
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.common.Gap
import org.philblandford.ui.layout.model.LayoutOption
import org.philblandford.ui.layout.model.LayoutOptionModel
import org.philblandford.ui.layout.viewmodel.LayoutOptionInterface
import org.philblandford.ui.layout.viewmodel.LayoutOptionViewModel
import org.philblandford.ui.theme.DialogTheme
import org.philblandford.ui.util.NumberSelector
import org.philblandford.ui.util.Stoppable

@Composable
fun LayoutOptions() {
  VMView(LayoutOptionViewModel::class.java) { model, iface, _ ->
    LayoutOptionsInternal(model, iface)
  }
}

@Composable
private fun LayoutOptionsInternal(model: LayoutOptionModel, iface: LayoutOptionInterface) {

  DialogTheme { modifier ->
    Column(
      modifier
        .fillMaxWidth().clickable {  }
        .background(MaterialTheme.colors.surface)) {
      model.options.forEach { layoutOption ->
        if (layoutOption.param == EventParam.OPTION_BARS_PER_LINE) {
          FixedBarsLine(layoutOption as LayoutOption<Int>, model, iface)
        } else {
          CBLine(layoutOption.string, layoutOption.param, layoutOption.value as Boolean) {
            iface.toggleOption(layoutOption.param)
          }
        }
      }
    }
  }
}

@Composable
private fun FixedBarsLine(
  layoutOption: LayoutOption<Int>,
  model: LayoutOptionModel,
  iface: LayoutOptionInterface
) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    val text = stringResource(id = R.string.option_fixed_bars)
    Checkbox(
      checked = layoutOption.value != 0,
      onCheckedChange = {
        val num = if (it) model.numFixedBars else 0
        iface.setNumBars(num)
      },
      colors = CheckboxDefaults.colors(
        checkmarkColor = MaterialTheme.colors.onSurface),
      modifier = Modifier.testTag(text)
    )
    Gap(0.2f)
    Text(text, fontSize = 15.sp)
    Gap(0.5f)
    Box(Modifier.align(Alignment.CenterVertically)) {
      Stoppable(enable = layoutOption.value != 0) {
        NumberSelector(min = 1, max = 16, num = model.numFixedBars, setNum = {
          iface.setNumBars(it)
        })
      }
    }

    Gap(0.3f)
  }
}

@Composable
private fun CBLine(textId: Int, param: EventParam, value: Boolean, update: () -> Unit) {
  Column {
    Row(verticalAlignment = Alignment.CenterVertically) {
      val text = stringResource(id = textId)
      ksLogt("$param $value")
      Checkbox(
        checked = value,
        onCheckedChange = { update() },
        colors = CheckboxDefaults.colors(
        checkmarkColor = MaterialTheme.colors.onSurface)
      )
      Gap(0.2f)
      Text(text, fontSize = 15.sp)
    }
    Gap(0.1f)
  }
}

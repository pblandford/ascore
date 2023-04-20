package org.philblandford.ui.insert.choose.compose

import GridSelection
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.philblandford.ui.R
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.common.block
import org.philblandford.ui.insert.choose.viewmodel.InsertChooseInterface
import org.philblandford.ui.insert.choose.viewmodel.InsertChooseModel
import org.philblandford.ui.insert.choose.viewmodel.InsertChooseViewModel
import org.philblandford.ui.util.SquareButton
import org.philblandford.ui.util.ThemeBox
import timber.log.Timber

@Composable
fun InsertChoosePanel() {
  VMView(InsertChooseViewModel::class.java) { state, iface, _ ->
    InsertChoosePanel(state, iface)
  }
}

@Composable
fun InsertChoosePanel(model: InsertChooseModel, iface: InsertChooseInterface) {
  ThemeBox(
    Modifier
      .fillMaxWidth()
      .wrapContentHeight()
      .border(1.dp, MaterialTheme.colors.onSurface)
  ) {
    Column(Modifier.fillMaxWidth()) {
      SearchBox(model, iface)

      Row(
        Modifier
          .fillMaxWidth()
          .height(block(3.5f)), verticalAlignment = Alignment.CenterVertically
      ) {
        Box(Modifier.weight(1f)) {
          SelectionGrid(model, iface, Modifier.align(Alignment.Center))
        }
        if (model.showNext) {
          NextPage(iface)
        }
      }
    }
  }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun SelectionGrid(
  model: InsertChooseModel,
  iface: InsertChooseInterface,
  modifier: Modifier = Modifier
) {
  var items by remember { mutableStateOf(model.items) }
  items = model.items

  AnimatedContent(items,
    transitionSpec =
    {
      (slideInHorizontally{ width -> width } with
              slideOutHorizontally { width -> -width } + fadeOut(animationSpec = tween(500))).using(
        SizeTransform(clip = false)
      )
    }, modifier = modifier
  ) { items ->

      GridSelection(
        images = items.map { it.drawable },
        rows = 2,
        columns = model.items.size / 2,
        onSelect = {
          model.items[it].let { item ->
            iface.select(item)
          }
        },
        tag = { model.items[it].helpTag },
        border = false,
        itemBorder = true,
        size = block(1.15f)
      )
    }
}

@Composable
private fun NextPage(
  iface: InsertChooseInterface,
  modifier: Modifier = Modifier
) {
  SquareButton(R.drawable.last_page, modifier, border = true, size = block(1.25f))
  { iface.nextPage() }
}

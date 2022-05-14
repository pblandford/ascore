package org.philblandford.ui.insert.compose

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import org.philblandford.ui.common.block
import org.philblandford.ui.insert.choose.viewmodel.InsertChooseInterface
import org.philblandford.ui.insert.choose.viewmodel.InsertChooseModel
import org.philblandford.ui.util.GridSelection
import org.philblandford.ui.util.SquareButton
import org.philblandford.ui.util.ThemeBox
import org.philblandford.ui.R
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.insert.choose.compose.SearchBox
import org.philblandford.ui.insert.choose.viewmodel.InsertChooseViewModel

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
      .testTag("InsertChoosePanel")
  ) {
    Column(Modifier.fillMaxWidth()) {
      SearchBox(model, iface)

      ConstraintLayout(
        Modifier
          .fillMaxWidth()
          .height(block(3.5f))
      ) {
        val (grid, next) = createRefs()
        SelectionGrid(model, iface,
          Modifier.constrainAs(grid) {
            start.linkTo(
              parent.start,
              5.dp
            ); centerVerticallyTo(parent)
          })
        if (model.showNext) {
          NextPage(iface,
            Modifier.constrainAs(next) { end.linkTo(parent.end, 5.dp); centerVerticallyTo(parent) })
        }
      }
    }
  }
}

@Composable
private fun SelectionGrid(
  model: InsertChooseModel,
  iface: InsertChooseInterface,
  modifier: Modifier = Modifier
) {
  Box(modifier) {
    GridSelection(
      images = model.items.map { it.drawable },
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

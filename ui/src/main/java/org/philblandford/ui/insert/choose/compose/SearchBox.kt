package org.philblandford.ui.insert.choose.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.philblandford.ui.common.block
import org.philblandford.ui.insert.choose.viewmodel.InsertChooseInterface
import org.philblandford.ui.insert.choose.viewmodel.InsertChooseModel
import org.philblandford.ui.util.BaseTextField2
import org.philblandford.ui.util.SquareImage

@Composable
fun SearchBox(model: InsertChooseModel, iface: InsertChooseInterface) {

  Column {
    Row() {
      SquareImage(android.R.drawable.ic_menu_search)
      BaseTextField2(
        "", onValueChange = { iface.helpText(it) },
        modifier = Modifier.size(block(5), block()),
        tag = "HelpTextField",
      )
    }
    model.searchItems.forEach { item ->
      Text(stringResource(item.string), Modifier.clickable {
        iface.select(item)
      })
    }
  }
}
package org.philblandford.ui.main.outer.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.philblandford.ui.common.block
import org.philblandford.ui.main.outer.model.DrawerItem
import org.philblandford.ui.main.outer.model.DrawerItemGroup
import org.philblandford.ui.main.outer.model.drawerItems
import org.philblandford.ui.theme.AscoreTheme
import org.philblandford.ui.util.SquareButton
import org.philblandford.ui.R


@Composable
fun DrawerItems(
  groups: List<DrawerItemGroup>,
  selected: (DrawerItem) -> Unit,
  close:()->Unit
) {
  Column(Modifier.fillMaxSize().background(MaterialTheme.colors.primary)) {
    SquareButton(R.drawable.cross, Modifier.offset(10.dp, 10.dp),
    size = block(0.75f)) {
      close()
    }
    Column(
      Modifier
        .width(200.dp)
        .testTag("SettingsDrawer")
        .offset(50.dp, 50.dp)
        .verticalScroll(rememberScrollState())
    ) {
      groups.map { group ->
        val opened = remember { mutableStateOf(false) }
        TextItem(group.nameId, Color.Transparent, "Category ${stringResource(group.nameId)}") {
          opened.value = !opened.value
        }
        if (opened.value) {
          group.items.map { setting ->

            val background = MaterialTheme.colors.primaryVariant
            TextItem(
              setting.nameId, background, "SubOption ${stringResource(setting.nameId)}",
              10.dp
            ) {
              selected(setting)
              opened.value = false
            }
          }
        }
      }
    }
  }
}

@Composable
private fun TextItem(
  stringResource: Int,
  background: Color,
  tag: String,
  offset: Dp = 0.dp,
  click: () -> Unit
) {
  Box(
    Modifier
      .background(background)
      .fillMaxWidth()
      .padding(5.dp)
      .clickable(onClick = click)
  ) {
    Text(
      stringResource(stringResource),
      modifier = Modifier
        .height(block())
        .background(background)
        .offset(offset)
        .testTag(tag)
        .align(Alignment.CenterStart),
      color = MaterialTheme.colors.onPrimary
    )
  }
}

@Composable
@Preview
private fun Preview() {
  AscoreTheme() {
    Box(
      Modifier
        .width(200.dp)
        .fillMaxHeight()
        .background(MaterialTheme.colors.primary)) {
      DrawerItems(drawerItems, {}) {}
    }
  }
}
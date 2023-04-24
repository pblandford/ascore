package org.philblandford.ui.insert.items.harmony.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.philblandford.kscore.log.ksLogt
import org.philblandford.ui.R
import org.philblandford.ui.common.block
import org.philblandford.ui.insert.common.compose.InsertVMView
import org.philblandford.ui.insert.items.harmony.model.HarmonyInsertModel
import org.philblandford.ui.insert.items.harmony.viewmodel.HarmonyInsertInterface
import org.philblandford.ui.insert.items.harmony.viewmodel.HarmonyInsertViewModel
import org.philblandford.ui.main.window.LocalWindowSizeClass
import org.philblandford.ui.main.window.compact
import org.philblandford.ui.util.Gap
import org.philblandford.ui.util.SquareButton
import org.philblandford.ui.util.TextGrid
import org.philblandford.ui.util.TextSpinner

@Composable
fun HarmonyInsert() {
  InsertVMView<HarmonyInsertModel,
      HarmonyInsertInterface,
      HarmonyInsertViewModel> { state, _, iface ->
    HarmonyInsertInternal(state, iface)
  }
}

@Composable
fun HarmonyInsertInternal(model: HarmonyInsertModel, iface: HarmonyInsertInterface) {
  if (LocalWindowSizeClass.current.compact()) {
    HarmonyInsertCompact(model, iface)
  } else {
    HarmonyInsertExpanded(model, iface)
  }
}

@Composable
fun HarmonyInsertCompact(model: HarmonyInsertModel, iface: HarmonyInsertInterface) {
  ConstraintLayout(Modifier.padding(10.dp)) {
    val (select, common, recent, leftRight, split) = createRefs()
    SelectRow(model, iface, Modifier.constrainAs(select) {})
    CommonChords(model, iface, Modifier.constrainAs(common) { top.linkTo(select.bottom, 10.dp) })
    RecentChords(
      model, iface,
      Modifier.constrainAs(recent) {
        top.linkTo(common.bottom, 10.dp)
      })
    LeftRight(iface, Modifier.constrainAs(leftRight) { top.linkTo(recent.bottom, 10.dp) })
    SplitButton(Modifier.constrainAs(split) {
      start.linkTo(leftRight.end, 50.dp); top.linkTo(recent.bottom, 10.dp)
    }, iface)
  }
}

@Composable
fun HarmonyInsertExpanded(model: HarmonyInsertModel, iface: HarmonyInsertInterface) {
  Column(Modifier.padding(10.dp)) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
      SelectRow(model, iface, Modifier)
      Row {
        SplitButton(Modifier, iface)
        Gap(1f)
        LeftRight(iface, Modifier)
      }
    }
    Row {
      CommonChords(model, iface, Modifier)
      Gap(1f)
      RecentChords(
        model, iface,
        Modifier
      )
    }
  }
}

@Composable
fun HarmonyInsertLandscape(model: HarmonyInsertModel, iface: HarmonyInsertInterface) {
  val block = block()
  ConstraintLayout(Modifier.padding(top = 15.dp)) {
    val (select, common, recent, leftRight, split) = createRefs()
    SelectRow(model, iface, Modifier.constrainAs(select) {})
    CommonChords(model, iface, Modifier.constrainAs(common) { top.linkTo(select.bottom, 10.dp) })
    RecentChords(
      model, iface,
      Modifier.constrainAs(recent) {
        start.linkTo(common.end, 10.dp)
        top.linkTo(select.bottom, 10.dp)
      })
    LeftRight(iface, Modifier.constrainAs(leftRight) { start.linkTo(select.end, block) })
    SplitButton(Modifier.constrainAs(split) {
      start.linkTo(leftRight.end, 50.dp)
    }, iface)
  }
}

@Composable
private fun SplitButton(
  modifier: Modifier,
  iface: HarmonyInsertInterface
) {
  SquareButton(
    R.drawable.split,
    tag = "Split",
    modifier = modifier,
    onLongPress = { iface.removeSplit() }) {
    iface.split()
  }
}

@Composable
private fun SelectRow(
  model: HarmonyInsertModel,
  iface: HarmonyInsertInterface,
  modifier: Modifier
) {
  Row(modifier) {
    ksLogt("${model.notes}")
    TextSpinner(strings = model.notes.map { it.letterString() },
      grid = true,
      gridRows = 4, gridColumns = 5,
      buttonBorder = true,
      tag = "Harmony",
      textAlign = TextAlign.Center,
      modifier = Modifier.size(block(2), block(1)),
      selected = {
        model.current.tone.letterString()
      }, onSelect = {
        iface.setTone(model.notes[it])
      }
    )
    Gap(10.dp)
    TextSpinner(strings = model.qualities,
      grid = true,
      gridRows = 4, gridColumns = 5,
      buttonBorder = true,
      tag = "Quality",
      textAlign = TextAlign.Center,
      textStyle = { MaterialTheme.typography.bodyMedium },
      modifier = Modifier.size(block(2), block(1)),
      itemModifier = Modifier.width(block(2)),
      selected = {
        model.current.quality
      }, onSelect = { iface.setQuality(model.qualities[it]) })
    Text("/", Modifier.width(block()), textAlign = TextAlign.Center)
    TextSpinner(strings = model.rootNotes.map { it.letterString() },
      grid = true,
      gridRows = 7, gridColumns = 3,
      buttonBorder = true,
      tag = "Root",
      textAlign = TextAlign.Center,
      modifier = Modifier.size(block(2), block(1)),
      itemModifier = Modifier.size(block(1.5)),
      selected = {
        model.current.root?.letterString() ?: ""
      }, onSelect = { iface.setRoot(model.rootNotes[it]) })
    Gap(10.dp)
    SquareButton(resource = R.drawable.add_box, tag = "AddButton", onClick = {
      iface.insertCurrent()
    })
  }
}

@Composable
private fun CommonChords(
  model: HarmonyInsertModel,
  iface: HarmonyInsertInterface,
  modifier: Modifier
) {
  Column(modifier) {
    Text(
      stringResource(R.string.common_chord),
      style = MaterialTheme.typography.bodyMedium.copy(fontSize = 10.sp)
    )
    TextGrid(
      model.common.map { it.toString() }, rows = 1, columns = 7,
      textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
    ) { selected ->
      iface.insertHarmony(model.common[selected])
    }
  }
}


@Composable
private fun RecentChords(
  model: HarmonyInsertModel,
  iface: HarmonyInsertInterface,
  modifier: Modifier
) {
  val text = model.recent.map { it.toString() } + (0 until (7 - model.recent.size)).map { " " }
  Column(modifier) {
    Text(
      stringResource(R.string.cached_chord),
      style = MaterialTheme.typography.bodyMedium.copy(fontSize = 10.sp)
    )
    TextGrid(
      text, rows = 1, columns = 7, border = true,
      textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
    ) { selected ->
      model.recent.getOrNull(selected)?.let { harmony ->
        iface.insertHarmony(harmony)
      }
    }
  }
}

@Composable
private fun LeftRight(
  iface: HarmonyInsertInterface,
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


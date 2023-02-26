package org.philblandford.ui.create.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.philblandford.kscore.api.Instrument
import com.philblandford.kscore.api.InstrumentGroup
import com.philblandford.kscore.api.NewScoreDescriptor
import kotlinx.coroutines.launch
import org.philblandford.ui.R
import org.philblandford.ui.common.block
import org.philblandford.ui.create.viewmodel.CreateInterface
import org.philblandford.ui.create.viewmodel.CreateModel
import org.philblandford.ui.util.DraggableList
import org.philblandford.ui.util.Gap
import org.philblandford.ui.util.SquareButton
import org.philblandford.ui.util.reorder

@Composable
internal fun CreateInstruments(
  availableInstruments: List<InstrumentGroup>,
  selectedInstruments: List<Instrument>,
  next: () -> Unit,
  cancel: () -> Unit,
  iface: CreateInterface
) {
  val scrollState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()

  CreateFrame(R.string.create_score_instruments, next, cancel) {
    Column {
      InstrumentList(
        Modifier
          .fillMaxWidth()
          .fillMaxHeight(0.3f), availableInstruments, object : CreateInterface by iface {
          override fun addInstrument(instrument: Instrument) {
            iface.addInstrument(instrument)
            coroutineScope.launch {
              scrollState.animateScrollToItem((selectedInstruments.size - 1).coerceAtLeast(0))
            }
          }
        })
      Gap(0.5f)
      Text(
        stringResource(R.string.selected_instruments),
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
      )
      SelectedInstruments(
        Modifier
          .fillMaxWidth()
          .fillMaxHeight(0.4f), selectedInstruments, iface, scrollState
      )
    }
  }
}

@Composable
private fun InstrumentList(
  modifier: Modifier, instrumentGroups: List<InstrumentGroup>,
  createInterface: CreateInterface
) {

  val expandedMap = remember { mutableStateOf((instrumentGroups.indices).associateWith { false }) }

  LazyColumn(
    modifier
      .padding(5.dp)
      .border(1.dp, MaterialTheme.colors.primary)
  ) {
    items(instrumentGroups.withIndex().toList()) { (idx, group) ->
      Column {
        Row {
          Text("+", Modifier.clickable {
            expandedMap.value = expandedMap.value + (idx to !(expandedMap.value[idx] ?: false))
          }, fontSize = 20.sp)
          Gap(5.dp)
          Text(group.name, fontSize = 17.sp)
        }
        if (expandedMap.value[idx] == true) {
          group.instruments.forEach {
            Text(
              it.name,
              Modifier
                .offset(20.dp)
                .padding(5.dp)
                .clickable {
                  createInterface.addInstrument(it)
                }, fontSize = 16.sp
            )
          }
        }
      }
    }
  }
}


@Composable
private fun SelectedInstruments(
  modifier: Modifier,
  instruments: List<Instrument>,
  iface: CreateInterface,
  listState: LazyListState
) {

  DraggableList(
    instruments.toList(),
    { instrument ->
      SelectedInstrument(instrument = instrument, false, iface)
    },
    iface::reorderInstruments,
    modifier
      .padding(5.dp)
      .border(1.dp, MaterialTheme.colors.primary),
    listState,
    { _, i -> i.name }
  )
}

@Composable
private fun SelectedInstrument(
  instrument: Instrument,
  selected: Boolean,
  iface: CreateInterface
) {

  val chosen = remember {
    mutableStateOf(instrument)
  }

  Box(
    Modifier
      .fillMaxWidth()
      .background(if (selected) MaterialTheme.colors.secondary else Color.Transparent)
  ) {
    ConstraintLayout(Modifier.fillMaxWidth()) {
      val (text, editButton) = createRefs()

      SquareButton(resource = android.R.drawable.ic_menu_edit, tag = "${instrument.label} edit",
        backgroundColor = Color.Transparent,
        size = block(0.5),
        modifier = Modifier.constrainAs(editButton) {
          end.linkTo(parent.end)
          centerVerticallyTo(parent)
        },
        onClick = { })
      Text(
        instrument.label,
        modifier = Modifier
          .testTag("${instrument.name} selected $chosen")
          .height(block())
          .padding(5.dp)
          .clickable(onClick = {

          })
          .constrainAs(text) { start.linkTo(parent.start) }
      )
    }
  }
}


@Composable
@Preview
private fun Preview() {

  var selectedInstruments by remember { mutableStateOf(listOf<Instrument>()) }
  val iface = object : CreateInterface by StubCreateInterface() {
    override fun addInstrument(instrument: Instrument) {
      selectedInstruments = selectedInstruments + instrument
    }

    override fun removeInstrument(instrument: Instrument) {
      selectedInstruments = selectedInstruments - instrument
    }

    override fun reorderInstruments(oldIndex: Int, newIndex: Int) {
      selectedInstruments = selectedInstruments.reorder(oldIndex, newIndex)
    }
  }
  CreateInstruments(
    (0..8).map { group ->
      InstrumentGroup("Group $group",
        (0..8).map { Instrument("Instrument $it", "", "Group $group", it, 0, listOf(), "", 0) })
    }, selectedInstruments,

    next = { /*TODO*/ }, cancel = { /*TODO*/ }, iface
  )
}
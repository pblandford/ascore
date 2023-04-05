package org.philblandford.ui.create.compose

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
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
import org.philblandford.ui.util.*
import timber.log.Timber

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

  WizardFrame(R.string.create_score_instruments, next, cancel) {
    Column {
      InstrumentList(
        Modifier
          .fillMaxWidth()
          .fillMaxHeight(0.5f), availableInstruments
      ) {
        iface.addInstrument(it)
        coroutineScope.launch {
          scrollState.animateScrollToItem((selectedInstruments.size - 1).coerceAtLeast(0))
        }
      }

      Gap(0.5f)
      Text(
        stringResource(R.string.selected_instruments),
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
      )
      SelectedInstruments(
        Modifier
          .fillMaxWidth()
          .weight(1f), selectedInstruments, iface, scrollState
      )
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
    instruments.withIndex().toList(),
    { instrument ->
      SwipeableInstrument(instrument.value, iface)
    },
    iface::reorderInstruments,
    modifier,
    listState,
    { _, i -> "${i.index} ${i.value}" }
  )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SwipeableInstrument(instrument: Instrument, iface: CreateInterface) {
  val state = rememberDismissState(
    confirmStateChange = {
      Timber.e("state change $it $instrument")
      if (it == DismissValue.DismissedToStart) {
        iface.removeInstrument(instrument)
        false
      } else {
        true
      }
    }
  )

  SwipeToDismiss(state,
    Modifier.fillMaxWidth(),
    directions = setOf(DismissDirection.EndToStart),
    dismissThresholds = { androidx.compose.material.FractionalThreshold(0.5f) },
    background = {
      SwipeBackground(state)
    }
  ) {
    SelectedInstrument(instrument, iface)
  }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SwipeBackground(state: DismissState) {
  val scale by animateFloatAsState(
    if (state.targetValue == DismissValue.Default) 0.75f else 1f
  )
  Box(
    Modifier
      .fillMaxSize()
      .background(Color.Red)
      .padding(horizontal = 20.dp),
    contentAlignment = Alignment.CenterEnd
  ) {
    Icon(
      Icons.Default.Delete,
      contentDescription = "Delete Icon",
      modifier = Modifier.scale(scale)
    )
  }
}


@Composable
private fun SelectedInstrument(
  instrument: Instrument,
  iface: CreateInterface
) {

  Box(
    Modifier
      .fillMaxWidth()
      .background(MaterialTheme.colors.surface)
  ) {
    Row(
      Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        instrument.label, modifier = Modifier
          .height(block())
          .padding(5.dp),
        fontSize = 17.sp
      )
      SquareButton(resource = android.R.drawable.ic_menu_edit, tag = "${instrument.label} edit",
        backgroundColor = Color.Transparent,
        size = block(0.5),
        onClick = { })
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
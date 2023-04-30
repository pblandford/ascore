package org.philblandford.ui.create.compose.compact

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissState
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.philblandford.kscore.api.Instrument
import com.philblandford.kscore.api.InstrumentGroup
import kotlinx.coroutines.launch
import org.philblandford.ui.R
import org.philblandford.ui.common.block
import org.philblandford.ui.create.compose.EditPartDialog
import org.philblandford.ui.create.compose.WizardFrame
import org.philblandford.ui.create.viewmodel.CreateInterface
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

  WizardFrame(
    R.string.create_score_instruments,
    { if (selectedInstruments.isNotEmpty()) next() },
    cancel
  ) {
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
      ) { idx, instrument ->
        iface.updateInstrument(idx, instrument)
      }
    }
  }
}

@Composable
private fun SelectedInstruments(
  modifier: Modifier,
  instruments: List<Instrument>,
  iface: CreateInterface,
  listState: LazyListState,
  update: (Int, Instrument) -> Unit
) {
  var editDialog by remember { mutableStateOf<IndexedValue<Instrument>?>(null) }

  editDialog?.let { (idx, instrument) ->
    EditPartDialog(instrument, {
      editDialog = editDialog?.copy(value = it)
    }) {
      editDialog?.value?.let { update(idx, it) }
      editDialog = null
    }
  }

  DraggableList(
    instruments.withIndex().toList(),
    { iv ->
      SwipeableInstrument(iv.value, iface) { editDialog = iv }
    },
    iface::reorderInstruments,
    true,
    modifier,
    listState,
    { _, i -> "${i.index} ${i.value}" }
  )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SwipeableInstrument(
  instrument: Instrument, iface: CreateInterface,
  edit: () -> Unit
) {
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
    dismissThresholds = { FractionalThreshold(0.5f) },
    background = {
      SwipeBackground(state)
    }
  ) {
    SelectedInstrument(instrument, edit)
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
  edit: () -> Unit
) {

  Box(
    Modifier
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.surface)
  ) {
    Row(
      Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        instrument.label, modifier = Modifier
          .height(block())
          .padding(5.dp),
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 17.sp
      )
      SquareButton(resource = android.R.drawable.ic_menu_edit, tag = "${instrument.label} edit",
        size = block(0.5),
        onClick = {
          edit()
        })
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
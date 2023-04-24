package org.philblandford.ui.create.compose

import android.graphics.pdf.PdfDocument.Page
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.philblandford.kscore.api.NewScoreDescriptor
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.types.PageSize
import org.philblandford.ui.R
import org.philblandford.ui.create.viewmodel.CreateInterface
import org.philblandford.ui.create.viewmodel.CreateModel
import org.philblandford.ui.theme.PopupTheme
import org.philblandford.ui.util.*

@Composable
internal fun CreateTempo(
  model: CreateModel,
  next: () -> Unit,
  cancel: () -> Unit,
  iface: CreateInterface
) {

  WizardFrame(R.string.create_score_tempo, {
    if (model.newScoreDescriptor.numBars > 0 &&
      model.newScoreDescriptor.tempo.bpm > 0
    ) {
      next()
    }
  }, cancel) {
    Column(Modifier.fillMaxSize()) {
      with(model.newScoreDescriptor) {
        TempoSelector(tempo) { iface.setTempo { it } }
        Gap(0.5f)
        Label(R.string.upbeatbar)
        UpbeatRow(upbeatEnabled, iface::setUpbeatEnabled, upBeat) { iface.setUpbeatBar { it } }
        Gap(0.5f)
        Row(
          Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Label(R.string.page_size)
            PageSizeRow(pageSize) { iface.setPageSize(it) }
          }
          Column {
            Label(R.string.num_bars)
            BarsRow(numBars) { iface.setNumBars(it) }
          }
        }
      }
    }
  }
}

@Composable
private fun Label(textId: Int) {
  Text(
    stringResource(textId),
    Modifier.padding(vertical = 5.dp),
    style = MaterialTheme.typography.bodyLarge
  )
}

@Composable
private fun UpbeatRow(
  upbeatEnabled: Boolean, setEnabled: (Boolean) -> Unit,
  timeSignature: TimeSignature, set: (TimeSignature) -> Unit
) {
  Row {
    Checkbox(
      upbeatEnabled, { setEnabled(it) },
      colors = CheckboxDefaults.colors(//checkedColor = MaterialTheme.colorScheme.onSurface,
        checkmarkColor = MaterialTheme.colorScheme.onSurface
      )
    )
    DimmableBox(!upbeatEnabled, Modifier.wrapContentWidth()) {
      CustomTimeSelector(timeSignature, { set(it) }, upbeatEnabled)
    }
  }
}

@Composable
private fun PageSizeRow(selected: PageSize, select: (PageSize) -> Unit) {
  Row {
    PageSize.values().take(4).forEach { pageSize ->
      val isSelected = selected == pageSize
      Text(
        pageSize.toString(),
        Modifier
          .background(
            if (isSelected) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.surface
          )
          .padding(5.dp)
          .clickable { select(pageSize) }, color = if (isSelected) MaterialTheme.colorScheme.surface
        else MaterialTheme.colorScheme.onSurface, fontSize = 20.sp
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BarsRow(numBars: Int, set: (Int) -> Unit) {
  val text = remember { mutableStateOf(numBars.toString()) }
  OutlinedTextField(
    text.value, { textVal ->
      val bars = textVal.toIntOrNull()
      text.value = textVal

      bars?.let {
        set(bars)
      } ?: run {
        set(0)
      }
    },
    Modifier
      .width(80.dp)
      .border(
        if (numBars < 1) BorderStroke(1.dp, Color.Red) else BorderStroke(
          0.dp,
          Color.Transparent
        )
      ), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
  )
}

@Composable
@Preview
private fun Preview() {
  PopupTheme {
    Box(Modifier.fillMaxSize()) {
      CreateKeyTimeSignature(
        CreateModel(NewScoreDescriptor(), listOf()),
        {},
        {},
        StubCreateInterface()
      )
    }
  }
}



package org.philblandford.ui.export.compose

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.philblandford.ascore.external.interfaces.ExportDestination
import com.philblandford.kscore.engine.types.ExportType
import kotlinx.coroutines.flow.Flow
import org.philblandford.ui.R
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.base.viewmodel.VMSideEffect
import org.philblandford.ui.common.block
import org.philblandford.ui.export.viewmodel.ExportInterface
import org.philblandford.ui.export.viewmodel.ExportModel
import org.philblandford.ui.export.viewmodel.ExportViewModel
import org.philblandford.ui.theme.DialogButton
import org.philblandford.ui.theme.DialogTheme

private fun ExportType.asStringRes(): Int {
  return when (this) {
    ExportType.MXML -> R.string.mxml
    ExportType.MIDI -> R.string.midi
    ExportType.MP3 -> R.string.mp3
    ExportType.WAV -> R.string.wav
    ExportType.JPG -> R.string.jpg
    ExportType.PDF -> R.string.pdf
    ExportType.SAVE -> R.string.save
    ExportType.ZIP -> -1
  }
}

@Composable
fun ExportFile(exportType: ExportType) {

  VMView(ExportViewModel::class.java) { state, iface, _ ->

    LaunchedEffect(exportType) {
      iface.setExportType(exportType)
    }

    DialogTheme {
      ExportFileInternal(it, state, iface)
    }
  }
}

@Composable
private fun ExportFileInternal(modifier: Modifier, model: ExportModel, iface: ExportInterface) {

  Box(modifier) {
    Main(model, iface)
    if (model.inProgress) {
      CircularProgressIndicator(Modifier.align(Alignment.Center))
    }
  }
}

@Composable
private fun Main(model: ExportModel, iface: ExportInterface) {
  Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {

    Text(stringResource(R.string.exporting_as, stringResource(id = model.exportType.asStringRes())))
    Spacer(Modifier.height(block()))
    OutlinedTextField(value = model.fileName,
      modifier = Modifier.testTag("FileNameTextField"),
      onValueChange = { tf ->
        iface.setFileName(tf)
      },
      label = { Text(stringResource(R.string.filename)) })
    Spacer(Modifier.height(block()))
    if (model.allParts != null) {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
        Text(
          stringResource(R.string.export_all_parts),
          modifier = Modifier.padding(horizontal = block(0.5f)),
          style = MaterialTheme.typography.body2
        )
        Checkbox(
          checked = model.allParts,
          onCheckedChange = {
            iface.toggleAllParts()
          },
        )
        //  HelpPopup(R.string.all_parts_help)
      }
      Spacer(Modifier.height(block()))
    }
    DialogButton(
      stringResource(
        R.string.export
      )
    )
    { iface.export(ExportDestination.SHARE) }
  }
}

@Composable
@Preview
private fun Preview() {
  val state = ExportModel("blah", ExportType.JPG, true, false)

  ExportFileInternal(Modifier, state, object : ExportInterface {
    override fun reset() {
      TODO("Not yet implemented")
    }

    override fun setFileName(name: String) {
      TODO("Not yet implemented")
    }

    override fun toggleAllParts() {
      TODO("Not yet implemented")
    }

    override fun export(destination: ExportDestination, uri: Uri?) {
      TODO("Not yet implemented")
    }

    override fun setExportType(exportType: ExportType) {
      TODO("Not yet implemented")
    }

    override fun getSideEffects(): Flow<VMSideEffect> {
      TODO("Not yet implemented")
    }


  })
}
package org.philblandford.ui.export.compose

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.philblandford.ascore.external.interfaces.ExportDestination
import com.philblandford.kscore.engine.types.ExportType
import org.philblandford.ui.LocalActivity
import org.philblandford.ui.export.viewmodel.ExportInterface
import org.philblandford.ui.export.viewmodel.ExportModel
import org.philblandford.ui.R
import org.philblandford.ui.common.Gap
import org.philblandford.ui.theme.PopupTheme
import org.philblandford.ui.util.SquareButton
import org.philblandford.ui.util.StyledText
import org.philblandford.ui.util.ThemeButton


@Composable
internal fun SaveOptions(
  model: ExportModel,
  iface: ExportInterface,
  destinations: Set<ExportDestination> = ExportDestination.values().toList().toSet()
) {
  SaveFilePicker(if (model.allParts == true) ExportType.ZIP else model.exportType, save = {
    iface.export(ExportDestination.EXTERNAL, it)
  }) { launch ->
    Column(
      Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.SpaceEvenly
    ) {
      if (destinations.contains(ExportDestination.PRIVATE)) {
        ButtonRow(R.string.load_local, R.string.save_local_help) {
          iface.export(ExportDestination.PRIVATE)
        }
        Gap(0.5)
      }
      if (destinations.contains(ExportDestination.PUBLIC)) {
        ButtonRow(R.string.load_app_external, R.string.save_external_help) {
          iface.export(ExportDestination.PUBLIC)
        }
        Gap(0.5)
      }
      if (destinations.contains(ExportDestination.EXTERNAL)) {
        ButtonRow(R.string.load_external, R.string.save_export_help) {
          launch(model.fileName)
        }
        Gap(0.5)
      }
      if (destinations.contains(ExportDestination.SHARE)) {
        ButtonRow(R.string.share, R.string.save_share_help) {
          iface.export(ExportDestination.SHARE)
        }
      }
    }
  }
}

@Composable
private fun ButtonRow(text: Int, helpText: Int, onClick: () -> Unit) {
  Box(
    Modifier
      .fillMaxWidth()
      .border(1.dp, MaterialTheme.colors.onSurface)
      .padding(10.dp)) {
    Text(stringResource(text),
      Modifier
        .fillMaxWidth()
        .clickable(onClick = { onClick() }))
    HelpPopup(helpText, Modifier.align(Alignment.CenterEnd))
  }
}

@Composable
internal fun HelpPopup(helpText:Int, modifier: Modifier = Modifier) {
  val showHelp = remember { mutableStateOf(false) }
  SquareButton(
    R.drawable.help,
    border = true,
    modifier = modifier
  ) { showHelp.value = true }
  if (showHelp.value) {
    Popup(onDismissRequest = { showHelp.value = false }) {
      PopupTheme {
        StyledText(helpText, fontSize = 15.sp)
      }
    }
  }
}

@Composable
private fun SavedFile(model: ExportModel, iface: ExportInterface) {
  val activity = LocalActivity.current
  Column(Modifier.fillMaxWidth()) {
    Text(
      stringResource(R.string.saved, model.fileName),
      Modifier.align(Alignment.CenterHorizontally)
    )
    Gap(0.5f)
    ThemeButton(
      R.string.ok,
      onClick = {   },
      modifier = Modifier.align(Alignment.CenterHorizontally)
    )
  }
}

package org.philblandford.ui.main.popup.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.github.zsoltk.compose.backpress.BackPressHandler
import com.github.zsoltk.compose.backpress.LocalBackPressHandler
import com.philblandford.kscore.engine.types.ExportType
import org.philblandford.ascore2.features.ui.model.LayoutID
import org.philblandford.ui.about.compose.About
import org.philblandford.ui.create.compose.CreateScore
import org.philblandford.ui.export.PrintFile
import org.philblandford.ui.export.compose.ExportFile
import org.philblandford.ui.layout.compose.LayoutOptions
import org.philblandford.ui.load.compose.LoadScore
import org.philblandford.ui.manual.compose.Manual
import org.philblandford.ui.play.compose.Mixer
import org.philblandford.ui.quickscore.compose.QuickScore
import org.philblandford.ui.save.compose.SaveScore
import org.philblandford.ui.settings.compose.Settings

@Composable
fun SettingsDialog(layoutID: LayoutID, dismiss: () -> Unit) {
  val backPressHandler = remember { BackPressHandler() }

  Dialog(
    onDismissRequest = {
      if (!backPressHandler.handle()) dismiss()
    },
    properties = DialogProperties(dismissOnBackPress = true)
  ) {
    CompositionLocalProvider(LocalBackPressHandler provides backPressHandler) {
      Box(Modifier.fillMaxSize()) {
        Box(
          Modifier
            .fillMaxSize()
            .clickable { dismiss() })
        Layout(layoutID, dismiss)
      }
    }
  }
}

@Composable
private fun Layout(layoutID: LayoutID, dismiss: () -> Unit) {
  when (layoutID) {
    LayoutID.NEW_SCORE -> {
      CreateScore(dismiss)
    }
    LayoutID.QUICK_SCORE -> {
      QuickScore(dismiss)
    }
    LayoutID.SAVE_SCORE -> {
      SaveScore(dismiss)
    }
    LayoutID.LOAD_SCORE -> {
      LoadScore(dismiss)
    }
    LayoutID.PRINT_SCORE -> {
      PrintFile()
      dismiss()
    }
    LayoutID.EXPORT_PDF -> {
      ExportFile(ExportType.PDF)
    }
    LayoutID.EXPORT_MIDI -> {
      ExportFile(ExportType.MIDI)
    }
    LayoutID.EXPORT_MXML -> {
      ExportFile(ExportType.MXML)
    }
    LayoutID.EXPORT_MP3 -> {
      ExportFile(ExportType.MP3)
    }
    LayoutID.EXPORT_WAV -> {
      ExportFile(ExportType.WAV)
    }
    LayoutID.EXPORT_SAVE -> {
      ExportFile(ExportType.SAVE)
    }
    LayoutID.LAYOUT_OPTIONS -> {
      LayoutOptions()
    }
    LayoutID.ABOUT -> {
      About()
    }
    LayoutID.MANUAL -> {
      Manual()
    }
    LayoutID.MIXER -> {
      Mixer()
    }
    LayoutID.SETTINGS_LAYOUT -> {
      Settings()
    }
    else -> {}
  }
}

@Composable
@Preview
private fun Preview() {
  Box(Modifier.fillMaxSize()) {
    SettingsDialog(LayoutID.NEW_SCORE) {

    }
  }
}
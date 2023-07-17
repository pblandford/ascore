package org.philblandford.ui.export.compose

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.testing.FakeReviewManager
import org.philblandford.ascore2.external.export.getExtension
import com.philblandford.ascore.external.interfaces.ExportDestination
import com.philblandford.kscore.engine.types.ExportType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import org.philblandford.ui.LocalActivity
import org.philblandford.ui.R
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.base.viewmodel.VMSideEffect
import org.philblandford.ui.common.block
import org.philblandford.ui.export.viewmodel.ExportEffect
import org.philblandford.ui.export.viewmodel.ExportInterface
import org.philblandford.ui.export.viewmodel.ExportModel
import org.philblandford.ui.export.viewmodel.ExportViewModel
import org.philblandford.ui.theme.DialogButton
import org.philblandford.ui.theme.DialogTheme
import org.philblandford.ui.util.Gap
import org.philblandford.ui.util.StandardAlert
import java.io.OutputStream

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
fun ExportFile(exportType: ExportType, dismiss: () -> Unit) {

  VMView(ExportViewModel::class.java) { state, iface, effects ->

    var showConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(exportType) {
      iface.setExportType(exportType)
    }

    LaunchedEffect(Unit) {
      effects.collectLatest { effect ->
        when (effect) {
          ExportEffect.Complete -> {
            showConfirm = true
          }

          ExportEffect.Error -> dismiss()
        }
      }
    }

    DialogTheme {
      if (showConfirm) {
        Confirm(it, state, dismiss)
      } else {
        ExportFileInternal(it, state, iface)
      }
    }
  }
}

@Composable
private fun Confirm(modifier: Modifier, state: ExportModel, dismiss: () -> Unit) {
  val activity = LocalActivity.current
  val reviewManager = activity?.let {
    remember {
      ReviewManagerFactory.create(activity)
    }
  }


  Column(
    modifier
      .fillMaxWidth()
      .padding(10.dp)
  ) {
    Text(stringResource(R.string.file_export_confirm, state.fileName))
    Gap(0.5f)
    Button({
      reviewManager?.requestReviewFlow()?.addOnCompleteListener { task ->
        reviewManager.launchReviewFlow(activity, task.result).addOnCompleteListener {
          dismiss()
        }
      } ?: run {
        dismiss()
      }
    }, Modifier.align(Alignment.CenterHorizontally)) {
      Text(stringResource(R.string.ok))
    }
  }
}

@Composable
private fun ExportFileInternal(
  modifier: Modifier, model: ExportModel, iface: ExportInterface
) {

  Box(modifier) {
    Main(model, iface)
    if (model.inProgress) {
      CircularProgressIndicator(Modifier.align(Alignment.Center))
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Main(model: ExportModel, iface: ExportInterface) {

  val context = LocalContext.current
  val launcher =
    rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { uri ->
      uri?.let {
        context.contentResolver.openOutputStream(uri)?.let { stream ->
          iface.getBytes(stream)
        }
      }
    }


  Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {

    Text(stringResource(R.string.exporting_as, stringResource(id = model.exportType.asStringRes())))
    Spacer(Modifier.height(block()))
    OutlinedTextField(value = model.fileName,
      modifier = Modifier.testTag("FileNameTextField"),
      onValueChange = { tf ->
        iface.setFileName(tf)
      },
      colors = TextFieldDefaults.outlinedTextFieldColors(unfocusedLabelColor = MaterialTheme.colorScheme.onSurface),
      label = { Text(stringResource(R.string.filename)) })
    Spacer(Modifier.height(block()))
    if (model.allParts != null) {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
        Text(
          stringResource(R.string.export_all_parts),
          modifier = Modifier.padding(horizontal = block(0.5f)),
          style = MaterialTheme.typography.bodyMedium
        )
        Checkbox(
          checked = model.allParts,
          onCheckedChange = {
            iface.toggleAllParts()
          },
        )
      }
      Spacer(Modifier.height(block()))
    }
    Row(
      Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {

      DialogButton(stringResource(R.string.export), enabled = model.fileName.isNotEmpty())
      {
        val extension = if (model.allParts == true) "zip" else model.exportType.getExtension()
        launcher.launch("${model.fileName}.${extension}")
      }
      DialogButton(stringResource(R.string.share), enabled = model.fileName.isNotEmpty()) {

        iface.export(ExportDestination.SHARE)
      }
    }
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

    override fun getBytes(outputStream: OutputStream) {
      TODO("Not yet implemented")
    }
  })
}
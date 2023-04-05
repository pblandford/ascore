package org.philblandford.ui.export.compose

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.philblandford.kscore.engine.types.ExportType
import org.philblandford.ui.export.model.getExtension
import org.philblandford.ui.export.model.getMimeType

private data class ExportDescr(val filename:String, val exportType: ExportType)

@Composable
fun SaveFilePicker(
  exportType: ExportType,
    save: (Uri) -> Unit,
    children: @Composable() ((String?) -> Unit) -> Unit
) {
  val fileName = remember { mutableStateOf<String?>(null) }

  val launcher =
      rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(exportType.getMimeType())) { uri ->
        uri?.let { save(it) }
      }

  fileName.value?.let {
    launcher.launch(it)
    fileName.value = null
  }

  children { name ->
    fileName.value =  "${name}.${exportType.getExtension()}"
  }
}



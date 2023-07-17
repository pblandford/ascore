package org.philblandford.ui.export.viewmodel

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.philblandford.ascore.external.interfaces.ExportDestination
import com.philblandford.kscore.engine.types.ExportType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.io.IOUtils
import org.philblandford.ascore2.external.export.getExtension
import org.philblandford.ascore2.features.export.ExportScore
import org.philblandford.ascore2.features.export.GetExportBytes
import org.philblandford.ascore2.features.instruments.GetInstruments
import org.philblandford.ascore2.features.save.GetFileName
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.base.viewmodel.BaseViewModel
import org.philblandford.ui.base.viewmodel.VMInterface
import org.philblandford.ui.base.viewmodel.VMModel
import org.philblandford.ui.base.viewmodel.VMSideEffect
import java.io.OutputStream

data class ExportModel(
  val fileName: String,
  val exportType: ExportType,
  val allParts: Boolean?,
  val inProgress: Boolean,

  ) : VMModel()

interface ExportInterface : VMInterface {
  fun setFileName(name: String)
  fun toggleAllParts()
  fun export(destination: ExportDestination, uri: Uri? = null)
  fun getBytes(outputStream: OutputStream)
  fun setExportType(exportType: ExportType)
}

sealed class ExportEffect : VMSideEffect() {
  object Complete : ExportEffect()
  object Error : ExportEffect()
}

class ExportViewModel(
  private val exportScore: ExportScore,
  private val getFileName: GetFileName,
  private val getInstruments: GetInstruments,
  private val getExportBytes: GetExportBytes
) : BaseViewModel<ExportModel, ExportInterface, ExportEffect>(), ExportInterface {

  override suspend fun initState(): Result<ExportModel> {
    return ExportModel(
      getFileName() ?: "", ExportType.JPG,
      if (getInstruments().size > 1) false else null, false
    ).ok()
  }

  override fun getInterface(): ExportInterface = this

  override fun setFileName(name: String) {
    update { copy(fileName = name) }
  }

  override fun toggleAllParts() {
    update { copy(allParts = allParts?.let { !it }) }
  }

  override fun export(destination: ExportDestination, uri: Uri?) {
    getState().value?.let { state ->
      update { copy(inProgress = true) }
      viewModelScope.launch(Dispatchers.IO) {
        tryAction({
          launchEffect(ExportEffect.Error)
        }) {

          exportScore(state.fileName, state.exportType, state.allParts ?: false, destination)
          launchEffect(ExportEffect.Complete)
        }
      }
    }
  }

  override fun getBytes(outputStream: OutputStream) {
    getState().value?.let { state ->
      viewModelScope.launch(Dispatchers.IO) {
        update { copy(inProgress = true) }
        tryAction({ launchEffect(ExportEffect.Error) }) {
          getExportBytes(state.exportType, state.allParts == true)?.let { bytes ->
            IOUtils.write(bytes, outputStream)
          }
          launchEffect(ExportEffect.Complete)
        }
      }
    }
  }

  override fun setExportType(exportType: ExportType) {
    update { copy(exportType = exportType) }
  }
}
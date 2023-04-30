package org.philblandford.ui.export.viewmodel

import android.net.Uri
import com.philblandford.ascore.external.interfaces.ExportDestination
import com.philblandford.kscore.engine.types.ExportType
import org.philblandford.ascore2.features.export.ExportScore
import org.philblandford.ascore2.features.instruments.GetInstruments
import org.philblandford.ascore2.features.save.GetFileName
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.base.viewmodel.BaseViewModel
import org.philblandford.ui.base.viewmodel.VMInterface
import org.philblandford.ui.base.viewmodel.VMModel
import org.philblandford.ui.base.viewmodel.VMSideEffect

data class ExportModel(
  val fileName:String,
  val exportType: ExportType,
  val allParts:Boolean?,
  val inProgress:Boolean
) : VMModel()

interface ExportInterface : VMInterface {
  fun setFileName(name:String)
  fun toggleAllParts()
  fun export(destination: ExportDestination, uri: Uri? = null)
  fun setExportType(exportType: ExportType)
}

class ExportViewModel(
  private val exportScore: ExportScore,
  private val getFileName: GetFileName,
  private val getInstruments: GetInstruments
  ) : BaseViewModel<ExportModel, ExportInterface, VMSideEffect>(), ExportInterface {

  override suspend fun initState(): Result<ExportModel> {
    return ExportModel(getFileName() ?: "", ExportType.JPG,
      if (getInstruments().size > 1) false else null, false).ok()
  }

  override fun getInterface(): ExportInterface  = this

  override fun setFileName(name: String) {
    update { copy(fileName = name) }
  }

  override fun toggleAllParts() {
    update { copy(allParts = allParts?.let { !it }) }
  }

  override fun export(destination: ExportDestination, uri: Uri?) {
    getState().value?.let { state ->
      exportScore(state.fileName, state.exportType, state.allParts ?: false, destination)
    }
  }

  override fun setExportType(exportType: ExportType) {
    update { copy(exportType = exportType) }
  }
}
package org.philblandford.ascore2.features.export

import ResourceManager
import android.net.Uri
import com.philblandford.ascore.external.export.Exporter
import com.philblandford.ascore.external.interfaces.ExportDestination
import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.engine.types.ExportType

class ExportScoreImpl(
  private val kScore: KScore,
  private val exporter: Exporter) : ExportScore {

  override fun invoke(fileName: String, exportType: ExportType, allParts: Boolean, exportDestination: ExportDestination, uri: Uri?) {
    kScore.getScore()?.let { score ->
      exporter.export(score, fileName, exportType, allParts, exportDestination, uri) {

      }
    }
  }
}
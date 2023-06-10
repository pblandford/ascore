package org.philblandford.ascore2.features.export

import android.net.Uri
import org.philblandford.ascore2.external.export.Exporter
import com.philblandford.ascore.external.interfaces.ExportDestination
import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.api.ProgressFunc
import com.philblandford.kscore.engine.types.ExportType

class ExportScoreImpl(
  private val kScore: KScore,
  private val exporter: Exporter
) : ExportScore {

  override fun invoke(fileName: String, exportType: ExportType, allParts: Boolean, exportDestination: ExportDestination, uri: Uri?,
  progress:ProgressFunc) {
    kScore.getScore()?.let { score ->
      exporter.export(score, fileName, exportType, allParts, exportDestination, uri, progress) {}
    }
  }
}
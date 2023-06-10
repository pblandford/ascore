package org.philblandford.ascore2.features.export

import org.philblandford.ascore2.external.export.Exporter
import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.engine.types.ExportType

class GetExportBytesImpl(private val kScore: KScore, private val exporter: Exporter): GetExportBytes {
  override fun invoke(exportType: ExportType, alLParts:Boolean): ByteArray? {
    return kScore.getScore()?.let { score ->
      exporter.getExportBytes(score, alLParts, score.getFilename() ?: "untitled", exportType)
    }
  }
}
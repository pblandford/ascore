package org.philblandford.ascore2.features.export

import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.types.ExportType

interface GetExportBytes {
  operator fun invoke(exportType:ExportType, allParts:Boolean):ByteArray?
}
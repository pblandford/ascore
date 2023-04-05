package org.philblandford.ascore2.features.export

import android.net.Uri
import com.philblandford.ascore.external.interfaces.ExportDestination
import com.philblandford.kscore.engine.types.ExportType

interface ExportScore {
  operator fun invoke(fileName:String, exportType: ExportType, allParts:Boolean,
                      exportDestination: ExportDestination, uri: Uri? = null)
}
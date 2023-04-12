package org.philblandford.ascore2.features.load.usecases

import android.net.Uri
import com.philblandford.ascore.external.interfaces.ScoreLoader
import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.saveload.Loader
import org.philblandford.ascore2.android.export.AndroidImporter
import java.net.URI

class ImportScoreImpl(
  private val importer: AndroidImporter,
  private val scoreLoader: ScoreLoader
) : ImportScore {

  override fun invoke(uri: Uri, progress: (Float) -> Unit) {
    importer.import(uri)?.let { importDescriptor ->
      scoreLoader.setImportedScore(importDescriptor.bytes, importDescriptor.importType) { t, s, p ->
        progress(p)
        false
      }
    }
  }
}
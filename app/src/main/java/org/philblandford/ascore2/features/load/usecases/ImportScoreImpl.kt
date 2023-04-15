package org.philblandford.ascore2.features.load.usecases

import android.net.Uri
import com.philblandford.ascore.external.interfaces.ScoreLoader
import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.api.ProgressFunc
import com.philblandford.kscore.saveload.Loader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.philblandford.ascore2.android.export.AndroidImporter
import timber.log.Timber
import java.net.URI

class ImportScoreImpl(
  private val importer: AndroidImporter,
  private val scoreLoader: ScoreLoader
) : ImportScore {

  override suspend fun invoke(uri: Uri, progress:
    (String,String,String,Float)->Unit
  ) {

    importer.import(uri)?.let { importDescriptor ->
      scoreLoader.setImportedScore(importDescriptor.bytes, importDescriptor.importType) { t, s, p ->
        progress(importDescriptor.fileName, t, s, p)
        false
      }
    }
  }
}
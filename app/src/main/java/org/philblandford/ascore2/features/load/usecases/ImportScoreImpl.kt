package org.philblandford.ascore2.features.load.usecases

import ResourceManager
import android.net.Uri
import com.philblandford.ascore.external.interfaces.ScoreLoader
import com.philblandford.kscore.api.InstrumentGetter
import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.api.ProgressFunc
import com.philblandford.kscore.engine.types.ImportType
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
  private val scoreLoader: ScoreLoader,
  private val resourceManager: ResourceManager,
  private val instrumentGetter: InstrumentGetter
) : ImportScore {

  override suspend fun invoke(
    uri: Uri, progress:
      (String, String, String, Float) -> Unit
  ) {

    importer.import(uri)?.let { importDescriptor ->

      when (importDescriptor.importType) {
        ImportType.MXL, ImportType.SAVE, ImportType.XML -> {

          scoreLoader.setImportedScore(
            importDescriptor.bytes,
            importDescriptor.importType
          ) { t, s, p ->
            progress(importDescriptor.fileName, t, s, p)
            false
          }
        }
        ImportType.SOUNDFONT -> {
          resourceManager.addSoundFont(importDescriptor.bytes, importDescriptor.fileName)
          instrumentGetter.refresh()
        }
        ImportType.TEXT -> {
          resourceManager.addTextFont(importDescriptor.bytes, importDescriptor.fileName)
        }
      }
    }
  }
}
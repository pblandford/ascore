package org.philblandford.ascore2.features.load.usecases

import ResourceManager
import android.content.Context
import com.philblandford.kscore.engine.types.FileSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.io.IOUtils
import timber.log.Timber

class InstallTemplatesImpl(private val resourceManager: ResourceManager,
                           private val context:Context) : InstallTemplates{

  private val coroutineScope = CoroutineScope(Dispatchers.IO)

  override fun invoke() {
    coroutineScope.launch {
      try {
        val templates = context.assets.list("templates")
        val existing = resourceManager.getSavedFiles(FileSource.TEMPLATE)
        templates?.toList()?.forEach { template ->
          if (!existing.any { it.name == template }) {
            val inputStream = context.assets.open("templates/$template")
            val bytes = IOUtils.toByteArray(inputStream)
            resourceManager.saveScore(template, bytes, FileSource.TEMPLATE)
          }
        }
      } catch (e: Exception) {
        Timber.e("Failed installing templates")
        Timber.e(e)
      }
    }
  }
}
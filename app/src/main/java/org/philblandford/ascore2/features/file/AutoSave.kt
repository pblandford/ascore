package org.philblandford.ascore2.features.file

import ResourceManager
import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.api.ProgressFunc
import com.philblandford.kscore.api.noProgress
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.FileSource
import com.philblandford.kscore.log.LogLevel
import com.philblandford.kscore.log.ksLog
import com.philblandford.kscore.log.ksLogv
import org.apache.commons.io.FileUtils
import timber.log.Timber
import java.io.File
import java.util.*
import kotlin.concurrent.schedule


class AutoSave(private val kScore: KScore, private val resourceManager: ResourceManager) {

  fun startAutoSave(seconds:Int = 60) {
    Timer().schedule(0, seconds.toLong()*1000) {
      try {
        saveAutoSave()
      } catch (e: Exception) {
        ksLog(LogLevel.ERROR, "Failed saving autosave", e)
      }
    }
  }

  fun tryAutoSave(progressFunc: ProgressFunc = noProgress): ByteArray? {
    return resourceManager.getSavedFilePaths(FileSource.AUTOSAVE)
      .maxByOrNull { File(it).lastModified() }?.let { autosave ->
        val file = File(autosave)
        progressFunc("Loading saved file ${file.name}", "", 0f)
        try {
          return FileUtils.readFileToByteArray(file)
        } catch (e:Exception) {
          return null
        }
      }
  }

  fun getAutosaveFileName(): String? {
    return null //Loader.getScoreFile(AUTOSAVE_NAME, FileSource.AUTOSAVE)?.name
  }

  fun getAutosaves(): List<String> {
    return resourceManager.getSavedFileNames(FileSource.AUTOSAVE)
  }

  internal fun saveAutoSave() {
    kScore.getScoreAsBytes()?.let { scoreBytes ->
      var title = kScore.getMeta(EventType.TITLE)
      if (title.isNullOrEmpty()) title = "untitled"
      Timber.e("Autosaving $title")
      resourceManager.saveScore("$title", scoreBytes, FileSource.AUTOSAVE)
    }
  }
}
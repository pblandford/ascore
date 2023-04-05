package org.philblandford.ascore2.features.file

import FileInfo
import ResourceManager
import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.api.ProgressFunc
import com.philblandford.kscore.api.noProgress
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.FileSource
import com.philblandford.kscore.log.LogLevel
import com.philblandford.kscore.log.ksLog
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
    return resourceManager.getSavedFiles(FileSource.AUTOSAVE)
      .maxByOrNull { it.accessTime }?.let { autosave ->
        val file = File(autosave.path)
        progressFunc("Loading saved file ${file.absolutePath}", "", 0f)
        try {
          return FileUtils.readFileToByteArray(file)
        } catch (e:Exception) {
          Timber.e("Could not load file $e")
          return null
        }
      }
  }

  fun getAutosaveFileName(): String? {
    return null //Loader.getScoreFile(AUTOSAVE_NAME, FileSource.AUTOSAVE)?.name
  }

  fun getAutosaves(): List<FileInfo> {
    return resourceManager.getSavedFiles(FileSource.AUTOSAVE)
  }

  private fun saveAutoSave() {
    kScore.getScoreAsBytes()?.let { scoreBytes ->
      var title = kScore.getCurrentFilename()
      if (title.isNullOrEmpty()) title = "untitled"
      Timber.e("Autosaving $title")
      resourceManager.saveScore("$title", scoreBytes, FileSource.AUTOSAVE)
    }
  }
}
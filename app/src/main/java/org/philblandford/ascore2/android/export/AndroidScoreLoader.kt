package org.philblandford.ascore2.android.export

import ResourceManager
import com.philblandford.ascore.external.export.mxml.`in`.reader.scoreFromMxml
import com.philblandford.ascore.external.interfaces.ScoreLoader
import com.philblandford.kscore.api.InstrumentGetter
import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.api.ProgressFunc
import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.types.FileSource
import com.philblandford.kscore.engine.types.ImportType
import com.philblandford.kscore.log.ksLoge
import com.philblandford.kscore.log.ksLogt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.IOUtils
import java.io.ByteArrayInputStream
import java.io.StringWriter
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class AndroidScoreLoader(
  private val kScore: KScore,
  private val resourceManager: ResourceManager,
  private val instrumentGetter: InstrumentGetter,
) : ScoreLoader {

  private var loading: Boolean = false

  override suspend fun loadScoreIfNone(progressFunc: ProgressFunc, onComplete: () -> Unit) {
    ksLogt("$loading ${kScore.getScore()}")
//    if (!loading && kScore.getScore() == null) {
//      progressFunc("Starting load", "", 0f)
//      ksLogt("Loading $loading")
//      loading = true
//      withContext(Dispatchers.Default) {
//        try {
//          autoSave.tryAutoSave(progressFunc)?.let { autoSave ->
//            kScore.setScore(autoSave, progressFunc)
//          } ?: run {
//            kScore.createDefaultScore()
//          }
//        } catch (e: Exception) {
//          ksLoge("Could not load autosave", e)
//          kScore.createDefaultScore()
//        }
//        autoSave.startAutoSave()
//        loading = false
//        ksLogt("Loading complete")
//        onComplete()
//      }
//    }
  }

  override suspend fun loadScore(name: String, fileSource: FileSource, progressFunc: ProgressFunc) {
    withContext(Dispatchers.Default) {
      resourceManager.loadScore(name, fileSource)?.let { bytes ->
        kScore.setScore(bytes, progressFunc)
      }
    }
  }

  override fun currentScore(): String? {
    return kScore.getCurrentFilename()
  }

  override fun haveScore(): Boolean {
    return !loading && kScore.getScore() != null
  }

  override  fun setImportedScore(
    bytes: ByteArray,
    importType: ImportType,
    progressFunc: ProgressFunc
  ) {
    if (!loading) {
      loading = true
      try {
        when (importType) {
          ImportType.SAVE -> setSaveScore(bytes, progressFunc)
          ImportType.XML -> setXmlScore(bytes, progressFunc)
          ImportType.MXL -> setMxlScore(bytes, progressFunc)
          else -> {
          }
        }
      } finally {
        loading = false
      }
    }
  }

  private fun setSaveScore(bytes: ByteArray, progressFunc: ProgressFunc) {
    kScore.setScore(bytes) { t1, t2, pc ->
      progressFunc(t1, t2, pc)
    }
  }

  private fun setXmlScore(bytes: ByteArray, progressFunc: ProgressFunc) {
    ksLogt("set Xml ${resourceManager.getDtdPath()}")
    scoreFromMxml(String(bytes), resourceManager.getDtdPath(), instrumentGetter) { t1, pc ->
      progressFunc(t1, "", pc)
    }?.let { score ->
      kScore.setScore(score) { t1, t2, pc ->
        progressFunc(t1, t2, pc)
      }
    } ?: run {

    }
  }

  private fun setMxlScore(bytes: ByteArray, progressFunc: ProgressFunc): Score? {
    val inputStream = ByteArrayInputStream(bytes)
    val zis = ZipInputStream(inputStream)
    var nextEntry: ZipEntry?
    do {
      nextEntry = zis.nextEntry
      val sw = StringWriter()
      if (nextEntry != null && nextEntry.name.split('/').firstOrNull() != "META-INF") {
        IOUtils.copy(zis, sw, "UTF-8")
        scoreFromMxml(sw.toString(), resourceManager.getDtdPath(), instrumentGetter) { t1, pc ->
          progressFunc(t1, "", pc)
        }?.let {
          kScore.setScore(it) { t1, t2, pc ->
            progressFunc(t1, t2, pc)
          }
        }
      }
    } while (nextEntry != null)
    return null
  }
}
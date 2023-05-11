package com.philblandford.ascore.external.interfaces

import android.app.Activity
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.philblandford.kscore.api.ProgressFunc
import com.philblandford.kscore.api.noProgress
import com.philblandford.kscore.engine.types.ExportType
import com.philblandford.kscore.engine.types.FileSource
import com.philblandford.kscore.engine.types.ImportType
import com.philblandford.kscore.engine.types.ScoreQuery
import java.io.File

enum class ExportDestination {
  PRIVATE, PUBLIC, EXTERNAL, SHARE
}


interface PdfCreator {
  fun createPdfBytes(scoreQuery: ScoreQuery): ByteArray?
  fun createPdfDocument(scoreQuery: ScoreQuery): PdfDocument?
}

interface PrinterIf {
  fun printScore(scoreQuery: ScoreQuery)
}

interface ExporterIf {
  fun share(bytes: ByteArray, filename: String, type: ExportType)
  fun printScore(scoreQuery: ScoreQuery)
  fun createPdf(scoreQuery: ScoreQuery): ByteArray?
  fun getTemporaryDir(): File?
}

interface ExternalSaver {
  fun setUri(uri: Uri)
  fun save(bytes:ByteArray)
}

interface ScoreLoader {
  suspend fun loadScoreIfNone(
    progressFunc: ProgressFunc = noProgress,
    onComplete: () -> Unit
  )

  suspend fun loadScore(
    name: String, fileSource: FileSource = FileSource.SAVE,
    progressFunc: ProgressFunc = noProgress
  )

  fun currentScore(): String?

  fun haveScore(): Boolean

  fun setImportedScore(
    bytes: ByteArray, importType: ImportType,
    progressFunc: ProgressFunc = noProgress
  )
}



interface DeviceConfiguration {
  fun tablet():Boolean
  val portrait:Boolean
}

interface ReviewRequester {
  fun requestReview(activity: Activity)
}
package org.philblandford.ascore2.android.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.philblandford.ascore.external.interfaces.ExporterIf
import com.philblandford.ascore.external.interfaces.PdfCreator
import com.philblandford.kscore.engine.types.ExportType
import com.philblandford.kscore.engine.types.ScoreQuery
import com.philblandford.kscore.log.ksLogd
import org.apache.commons.io.FileUtils
import java.io.File

class AndroidExporter(
  private val pdfCreator: PdfCreator,
  private val context: Context
) : ExporterIf {


  override fun share(
    bytes: ByteArray, filename: String, type: ExportType
  ) {
    ksLogd("export byte array of ${bytes.size}")

    val directory = context.cacheDir

    val outputFile = File(directory, filename)
    try {
      FileUtils.writeByteArrayToFile(outputFile, bytes)
    } catch (e: Exception) {
      throw(Exception("Failed writing to ${outputFile.absolutePath}", e))
    }
    share(
      getMimeType(type),
      Uri.parse("content://com.philblandford.ascore2.provider/$filename")
    )
  }

  override fun getTemporaryDir(): File? {
    return context.cacheDir
  }

  private fun getMimeType(exportType: ExportType): String {
    return when (exportType) {
      ExportType.PDF -> "application/pdf"
      ExportType.JPG -> "image/jpeg"
      ExportType.MIDI -> "application/x-midi"
      ExportType.SAVE -> "application/octet-stream"
      ExportType.ZIP -> "application/zip"
      ExportType.MXML -> "application/xml"
      ExportType.MP3 -> "audio/mp3"
      ExportType.WAV -> "audio/wav"
    }
  }

  override fun printScore(scoreQuery: ScoreQuery) {
  }

  override fun createPdf(scoreQuery: ScoreQuery): ByteArray? {
    return pdfCreator.createPdfBytes(scoreQuery)
  }

  private fun share(mimeType: String, outputFile: Uri) {
    doShare(mimeType, outputFile)
  }

  private fun doShare(mime: String, src: Uri) {
    val shareIntent = Intent().apply {
      action = Intent.ACTION_SEND
      putExtra(Intent.EXTRA_STREAM, src)
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      type = mime
    }
    context.startActivity(shareIntent)
  }

}
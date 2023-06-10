package org.philblandford.ascore2.external.export

import ResourceManager
import android.net.Uri
import org.philblandford.ascore2.external.export.mp3.writeAudio
import org.philblandford.ascore2.external.export.mxml.out.createMxml
import com.philblandford.ascore.external.export.out.writeMidi
import com.philblandford.ascore.external.interfaces.ExportDestination
import com.philblandford.ascore.external.interfaces.ExporterIf
import com.philblandford.ascore.external.interfaces.ExternalSaver
import com.philblandford.kscore.api.InstrumentGetter
import com.philblandford.kscore.api.ProgressFunc
import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.eventadder.util.addEventToMap
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.saveload.Saver
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

fun ExportType.getExtension(): String {
  return when (this) {
    ExportType.PDF -> "pdf"
    ExportType.JPG -> "jpg"
    ExportType.MIDI -> "mid"
    ExportType.SAVE -> "asc"
    ExportType.MXML -> "xml"
    ExportType.MP3 -> "mp3"
    ExportType.WAV -> "wav"
    ExportType.ZIP -> "zip"
    else -> ""
  }
}

class Exporter(
  private val nativeExporter: ExporterIf,
  private val resourceManager: ResourceManager,
  private val externalSaver: ExternalSaver,
  private val instrumentGetter: InstrumentGetter
) {

  private val exportFuncs = mapOf<ExportType, (Score) -> ByteArray?>(
    ExportType.PDF to { s -> exportPDF(s) },
    ExportType.MIDI to { s -> exportMIDI(s) },
    ExportType.MXML to { s -> exportMXML(s) },
    ExportType.MP3 to { s -> exportMP3(s) },
    ExportType.WAV to { s -> exportWAV(s) },
    ExportType.SAVE to { s -> exportSave(s) }
  )

  fun export(
    score: Score,
    filename: String,
    exportType: ExportType,
    allParts: Boolean = false,
    exportDestination: ExportDestination,
    uri: Uri? = null,
    progress: ProgressFunc = { _, _, _ -> false },
    onComplete: () -> Unit
  ) {

    val safeFileName = filename.replace(Regex("[:\\\\/*?|<>]"), "")

    uri?.let { externalSaver.setUri(it) }

    getExportBytes(score, allParts, safeFileName, exportType)?.let { bytes ->
      doExport(bytes, exportDestination, exportType, filename)
      onComplete()
    }
  }

  fun getExportBytes(
    score: Score, allParts: Boolean, filename: String,
    exportType: ExportType
  ): ByteArray? {
    return if (allParts) {
      getZipBytes(score, filename, exportType)
    } else {
      exportFuncs[exportType]?.let { func ->
        func(score)
      }
    }
  }

  private fun doExport(
    bytes: ByteArray, exportDestination: ExportDestination,
    exportType: ExportType, filename: String
  ) {
    when (exportDestination) {
      ExportDestination.SHARE -> nativeExporter.share(
        bytes,
        "$filename.${exportType.getExtension()}",
        exportType
      )

      ExportDestination.PRIVATE -> resourceManager.saveScore(filename, bytes, FileSource.SAVE)
      ExportDestination.PUBLIC -> resourceManager.saveScore(filename, bytes, FileSource.EXTERNAL)
      ExportDestination.EXTERNAL -> externalSaver.save(bytes)
    }
  }

  internal fun printScore(score: ScoreQuery) {
    nativeExporter.printScore(score)
  }

  private fun getZipBytes(
    score: Score,
    filename: String,
    exportType: ExportType
  ): ByteArray? {
    return exportFuncs[exportType]?.let { func ->
      val bytesNames = getScores(score).mapNotNull { (score, name) ->
        func(score)?.let { it to name }
      }
      createZip(bytesNames, exportType, filename)
    }
  }

  private fun exportPDF(score: Score): ByteArray? {
    return nativeExporter.createPdf(score)
  }

  private fun exportMIDI(score: Score): ByteArray? {
    return writeMidi(score, instrumentGetter)
  }

  private fun exportMP3(score: Score): ByteArray? {
    return exportSound(score, ExportType.MP3)
  }

  private fun exportWAV(score: Score): ByteArray? {
    return exportSound(score, ExportType.WAV)
  }

  private fun exportSound(score: Score, type: ExportType): ByteArray? {
    val path = "${resourceManager.getSoundFontPath()}/${resourceManager.getDefaultSoundFont()}"
    val outputType = when (type) {
      ExportType.MIDI -> com.philblandford.mp3converter.api.ExportType.MIDI
      ExportType.MP3 -> com.philblandford.mp3converter.api.ExportType.MP3
      ExportType.WAV -> com.philblandford.mp3converter.api.ExportType.WAV
      else -> com.philblandford.mp3converter.api.ExportType.MP3
    }
    return writeAudio(score, instrumentGetter, outputType, path)
  }

  private fun exportMXML(score: Score): ByteArray? {
    return createMxml(score)?.toByteArray()
  }

  private fun exportSave(score: Score): ByteArray? {
    return Saver().createSaveScore(score)
  }

  private fun createZip(
    files: Iterable<Pair<ByteArray, String>>,
    exportType: ExportType,
    name: String
  ): ByteArray? {

    val fullName = "$name-$exportType"

    return nativeExporter.getTemporaryDir()?.let { tmpDir ->
      val ext = exportType.getExtension()

      val outputDir = File(tmpDir, fullName)
      outputDir.mkdirs()
      val outputFile = File(outputDir, "$fullName.zip")
      val zipOutput = ZipOutputStream(FileOutputStream(outputFile))
      zipOutput.putNextEntry(ZipEntry("$fullName/"))
      files.withIndex().forEach { iv ->
        zipOutput.putNextEntry(ZipEntry("$fullName/$name-${iv.value.second}.$ext"))
        zipOutput.write(iv.value.first)
        zipOutput.closeEntry()
      }
      zipOutput.close()
      FileInputStream(outputFile).use { IOUtils.toByteArray(it) }
    }
  }

  private fun getScores(score: Score): Iterable<Pair<Score, String>> {

    val used = mutableMapOf<String, Int>()
    val range = listOf(0).plus(score.allParts(false))

    return range.map { part ->
      val partScore =
        score.addEventToMap(
          EventType.UISTATE,
          paramMapOf(EventParam.SELECTED_PART to part),
          eZero()
        )
      var name = partScore.selectedPartName() ?: "All"
      val numUsed = used[name] ?: 0
      used.put(name, numUsed + 1)
      if (numUsed != 0) {
        name = "$name-$numUsed"
      }
      partScore to name
    }
  }
}
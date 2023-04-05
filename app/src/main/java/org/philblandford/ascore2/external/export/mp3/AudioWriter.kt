package org.philblandford.ascore2.external.export.mp3

import com.philblandford.ascore.external.export.out.writeMidi
import com.philblandford.kscore.api.InstrumentGetter
import com.philblandford.kscore.engine.types.ExportType
import com.philblandford.kscore.engine.types.ScoreQuery
import com.philblandford.mp3converter.api.MidiToMp3Converter
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

fun writeAudio(scoreQuery: ScoreQuery, instrumentGetter: InstrumentGetter, exportType: ExportType, soundFontPath: String): ByteArray {
  val midiFile = writeMidi(scoreQuery, instrumentGetter)

  val converter = MidiToMp3Converter(soundFontPath)
  val inputStream = ByteArrayInputStream(midiFile)
  val outputStream = ByteArrayOutputStream()
  val libraryExportType = if (exportType == ExportType.WAV) {
    com.philblandford.mp3converter.api.ExportType.WAV
  } else {
    com.philblandford.mp3converter.api.ExportType.MP3
  }
  return runBlocking {
    converter.convert(inputStream, outputStream, libraryExportType) {}
    outputStream.toByteArray()
  }
}
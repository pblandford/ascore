package com.philblandford.mp3converter.api

import com.philblandford.mp3converter.ISampler
import com.philblandford.mp3converter.engine.encode.IEncoder
import com.philblandford.mp3converter.engine.encode.LameEncoder
import com.philblandford.mp3converter.engine.file.convertMidiToMp3
import com.philblandford.mp3converter.engine.file.convertMidiToWave
import com.philblandford.mp3converter.engine.sample.FluidConvertSampler
import com.philblandford.mp3converter.engine.sample.FluidSampler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.IOUtils
import java.io.InputStream
import java.io.OutputStream

enum class ExportType {
  MP3,
  WAV,
  MIDI
}

class MidiToMp3Converter(soundFontPath: String) {

  private val encoder: IEncoder = LameEncoder()
  private val sampler: ISampler = FluidConvertSampler(soundFontPath)

  suspend fun convert(
    inputStream: InputStream, outputStream: OutputStream, exportType: ExportType,
    updateProgress: (Int) -> Unit
  ) {
    withContext(Dispatchers.IO) {
      val bytes = IOUtils.toByteArray(inputStream)

      when (exportType) {
        ExportType.MP3 -> {
          encoder.start()
          convertMidiToMp3(bytes, sampler, encoder, { outputStream.write(it) }, updateProgress)
          encoder.finish()
        }
        ExportType.WAV -> convertMidiToWave(
          bytes,
          sampler, { outputStream.write(it) },
          updateProgress
        )
        else -> {
        }
      }
    }
  }
}
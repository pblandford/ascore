package com.philblandford.mp3converter

import android.net.Uri
import android.os.Parcelable
import com.philblandford.mp3converter.api.ExportType
import com.philblandford.mp3converter.engine.file.input.MidiEvent
import java.io.OutputStream

enum class DestinationType {
  LOCAL,
  SHARE
}

// Microseconds
typealias Ms = Long
typealias Delta = Int

interface ISampler {
  fun open()
  fun close()
  fun passEvent(midiEvent: MidiEvent)
  fun getSample(length:Ms):List<Short>
  fun getPresets():List<Preset>
  fun numChannels():Int
}

data class Preset(val name: String, val group:String, val program:Int, val soundFont:String, val bank:Int)

data class ConvertOptions(val exportType: ExportType, val midiFile: MidiFileDescr)
data class MidiFileDescr(val id: Long, val name: String, val uri: Uri)

data class OutputFileDescr(val uri: Uri, val displayPath: String, val outputStream: OutputStream)

interface FileGetter {
  fun getMidiFiles(): List<MidiFileDescr>
  fun createNewFile(name: String, type: ExportType): OutputFileDescr?
  fun finishSave(uri: Uri)
  fun deleteFile(uri: Uri)
}

interface Converter {
  suspend fun convertFile(
    midiFile: MidiFileDescr, exportType: ExportType, outputStream: OutputStream,
    updateProgress: (Int) -> Unit
  )
}
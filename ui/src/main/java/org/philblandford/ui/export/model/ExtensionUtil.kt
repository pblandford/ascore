package org.philblandford.ui.export.model

import com.philblandford.kscore.engine.types.ExportType

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

fun ExportType.getMimeType(): String {
  return when (this) {
    ExportType.PDF -> "application/pdf"
    ExportType.JPG -> "image/jpg"
    ExportType.MIDI -> "audio/midi"
    ExportType.SAVE -> "application/octet-stream"
    ExportType.MXML -> "application/xml"
    ExportType.MP3 -> "audio/mpeg"
    ExportType.WAV -> "audio/wav"
    ExportType.ZIP -> "application/zip"
    else -> "application/octet-stream"
  }
}
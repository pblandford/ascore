package org.philblandford.ascore2.android.export

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.philblandford.kscore.engine.types.ImportType
import com.philblandford.kscore.log.ksLogt
import com.philblandford.kscore.saveload.VERSION_MARKER
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import java.io.File

data class ImportDescriptor(val bytes: ByteArray, val fileName: String, val importType: ImportType)

class AndroidImporter(private val context: Context) {

  fun import(uri: Uri?): ImportDescriptor? {

    return uri?.let {
      ksLogt("$uri ${it.scheme}")
      val name = getName(uri, context)
      ksLogt("name $name")

      getBytesFromUri(uri, context)?.let { bytes ->

        getImportType(uri, context, bytes)?.let { importType ->
          ksLogt("$name $importType")

          ImportDescriptor(bytes, name ?: "", importType)
        }
      } ?: run {
        throw Exception("Could not determine file type for $name")
      }
    }
  }

  private fun getImportType(uri: Uri, context: Context, bytes: ByteArray): ImportType? {
    return getExtension(uri, context)?.let { ext ->
      when (ext.toLowerCase()) {
        "sf2" -> ImportType.SOUNDFONT
        "ttf", "otf" -> ImportType.TEXT
        "asc" -> ImportType.SAVE
        "xml" -> ImportType.XML
        "mxl" -> ImportType.MXL
        else -> guessFileType(bytes)
      }
    }
  }

  private fun guessFileType(bytes: ByteArray): ImportType? {
    if (bytes.take(4) == VERSION_MARKER.toByteList()) {
      return ImportType.SAVE
    }
    if (String(bytes.take(4).toByteArray()) == "<?xml") {
      return ImportType.XML
    }
    if (bytes.take(4) == 0x04034b50.toByteList()) {
      return ImportType.MXL
    }
    return null
  }

  private fun Int.toByteList():List<Byte> {
    return listOf(
    ((this shr 24) and 0xff).toByte(),
    ((this shr 16) and 0xff).toByte(),
    ((this shr 8) and 0xff).toByte(),
    (this and 0xff).toByte()
    )
  }

  private fun getExtension(uri: Uri?, context: Context): String? {
    return when (uri?.scheme) {
      "content" -> getExtensionContent(uri, context)
      "file" -> getExtensionFile(uri, context)
      else -> null
    }
  }

  private fun getName(uri: Uri?, context: Context): String {
    return when (uri?.scheme) {
      "content" -> getNameContent(uri, context)
      "file" -> getNameFile(uri, context)
      else -> ""
    }
  }

  private fun getExtensionContent(uri: Uri, context: Context): String {
    val cursor = context.contentResolver.query(
      uri, null, null,
      null, null
    ) ?: throw IllegalArgumentException("Could not find file from uri $uri")
    cursor.use {
      val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
      cursor.moveToFirst()
      return cursor.getString(nameIndex).split(".").last()
    }
  }

  private fun getExtensionFile(uri: Uri, context: Context): String {
    return FilenameUtils.getExtension(uri.path)
  }

  private fun getNameContent(uri: Uri, context: Context): String {
    val cursor = context.contentResolver.query(
      uri, null, null,
      null, null
    ) ?: throw IllegalArgumentException("Could not find file from uri $uri")
    cursor.use {
      val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
      cursor.moveToFirst()
      val fullName = cursor.getString(nameIndex)
      val ret = fullName.split(".").dropLast(1).lastOrNull() ?: fullName
      cursor.close()
      return ret
    }
  }

  private fun getNameFile(uri: Uri, context: Context): String {
    return FilenameUtils.getBaseName(uri.path)
  }

  private fun getBytesFromUri(uri: Uri, context: Context): ByteArray? {
    val inputStream = when (uri.scheme) {
      "content" -> context.contentResolver.openInputStream(uri)
      "file" -> FileUtils.openInputStream(File(uri.path))
      else -> null
    }
    return inputStream?.let { ist -> IOUtils.toByteArray(ist) }
  }
}

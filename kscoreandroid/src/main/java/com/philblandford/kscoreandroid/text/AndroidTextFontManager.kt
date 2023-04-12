package com.philblandford.kscoreandroid.text

import Preferences
import TextFontManager
import android.content.Context
import com.philblandford.kscore.engine.core.area.factory.TextType
import com.philblandford.kscore.log.ksLogt
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class AndroidTextFontManager(
  private val context: Context
) : TextFontManager {

  private val storageDir = context.filesDir
  private var textFontDir: File
  private val DEFAULT_TEXT_FONT = "default"

  init {
    textFontDir = File(storageDir, "TextFont")
    textFontDir.mkdirs()
    loadFromAssets()
  }


  override fun addTextFont(bytes: ByteArray, name: String) {
    val dest = File(textFontDir, name)
    FileUtils.writeByteArrayToFile(dest, bytes)
  }

  override fun getTextFont(name: String): InputStream? {
    for (ext in listOf("otf", "ttf")) {
      try {
        return context.assets?.open("$name.$ext")
      } catch (e: Exception) {
        try {
          return FileInputStream(File(textFontDir, "$name.$ext"))
        } catch (e: Exception) {
          ksLogt("Found naff all")
        }
      }
    }
    return null
  }

  override fun getTextFonts(): List<String> {
    return textFontDir.list()?.toList()?.map { FilenameUtils.removeExtension(it) } ?: listOf()
  }

  override fun deleteTextFonts() {
    textFontDir.listFiles()?.forEach { it.delete() }
    loadFromAssets()
  }

  override fun getTextFontPath(name: String): String? {
    val file = File(textFontDir, name)
    return if (file.exists()) {
      file.absolutePath
    } else File(textFontDir, DEFAULT_TEXT_FONT).absolutePath
  }

  override fun getDefaultTextFont(): String {
    return DEFAULT_TEXT_FONT
  }

  override fun getTextFontPath(
    name: String?,
    textType: TextType?
  ): String? {
    val font = getTextFont(name, textType)
    return getTextFontPath(font)
  }

  private fun getTextFont(font: String?, textType: TextType?): String {
    return font ?:
      when (textType) {
        TextType.SYSTEM -> "tempo"
        TextType.EXPRESSION -> "expression"
        else -> DEFAULT_TEXT_FONT
    }
  }

  private fun loadFromAssets() {
    context.assets.list("textfont")?.forEach {
      writeAsset("textfont/$it", textFontDir)
    }
  }

  private fun writeAsset(path: String, destination: File) {
    val iStream = context.assets.open(path)
    val bytes = IOUtils.toByteArray(iStream)
    val destFile = File(destination, FilenameUtils.getBaseName(path))
    FileUtils.writeByteArrayToFile(destFile, bytes)
  }

}
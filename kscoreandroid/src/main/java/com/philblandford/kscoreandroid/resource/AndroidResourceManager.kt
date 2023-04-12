package com.philblandford.kscoreandroid.resource

import FileInfo
import ResourceManager
import TextFontManager
import android.content.Context
import com.philblandford.kscore.engine.types.FileSource
import com.philblandford.kscore.log.ksLogt
import com.philblandford.kscore.resource.ImageDesc
import com.philblandford.kscoreandroid.sound.DEFAULT_SOUNDFONT
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import org.jetbrains.annotations.TestOnly
import java.io.File
import java.io.FileNotFoundException
import java.nio.charset.Charset


private val imageDescMap = mutableMapOf<String, ImageDesc>()

private const val SAVE_EXTENSION = "asc"
private const val INSTRUMENT_DATA = "instruments.json"
private const val INSTRUMENT_BASE_DATA = "instruments.txt"
private const val DTD = "partwise.dtd"

open class AndroidResourceManager(
  private val context: Context,
  private val textFontManager: TextFontManager
) : TextFontManager by textFontManager, ResourceManager {


  private val storageDir = context.filesDir
  private val externalStorageDir = context.getExternalFilesDir(null)
  private var bitmapDir: File
  private var soundFontDir: File
  private var saveDir: File
  private var autoSaveDir: File
  private var templateDir: File
  private var instrumentDataDir: File
  private var previousBaseDescriptions: String? = null

  init {
    bitmapDir = File(storageDir, "Bitmap")
    bitmapDir.mkdirs()

    soundFontDir = File(storageDir, "SoundFont")
    soundFontDir.mkdirs()
    saveDir = File(storageDir, "save")
    saveDir.mkdirs()
    autoSaveDir = File(storageDir, "autosave")
    autoSaveDir.mkdirs()
    templateDir = File(storageDir, "template")
    templateDir.mkdirs()
    instrumentDataDir = File(storageDir, "instrumentData")
    instrumentDataDir.mkdirs()
    loadFromAssets()
    loadFromCacheDir()
  }


  override fun getImage(key: String): ImageDesc? {
    return imageDescMap[key]
  }

  override fun getAllImages(): Iterable<ImageDesc> {
    return imageDescMap.values
  }

  override fun getBaseInstrumentDescriptions(): String? {
    val file = File(instrumentDataDir, INSTRUMENT_BASE_DATA)
    return try {
      FileUtils.readFileToString(file, Charset.defaultCharset())
    } catch (e: Exception) {
      null
    }
  }

  override fun getPreviousBaseInstrumentDescriptions(): String? {
    return previousBaseDescriptions
  }

  override fun getUserInstrumentDescriptions(): String? {
    val file = File(instrumentDataDir, INSTRUMENT_DATA)
    return try {
      FileUtils.readFileToString(file, Charset.defaultCharset())
    } catch (e: FileNotFoundException) {
      null
    }
  }

  override fun saveInstrumentDescriptions(string: String) {
    val file = File(instrumentDataDir, INSTRUMENT_DATA)
    FileUtils.writeStringToFile(file, string, Charset.defaultCharset())
  }

  override fun clearInstrumentDescriptions() {
    File(instrumentDataDir, INSTRUMENT_DATA).delete()
  }

  override fun getDtdPath(): String {
    return File(instrumentDataDir, DTD).absolutePath
  }

  override fun getSavedFiles(fileSource: FileSource): List<FileInfo> {
    return fileSource.getDir()?.let { dir ->
      dir.listFiles()?.map { file ->
        FileInfo(file.name, file.absolutePath, fileSource, file.lastModified())
      }
    } ?: listOf()
  }

  override fun saveScore(name: String, bytes: ByteArray, fileSource: FileSource) {
    fileSource.getDir()?.let { saveFile(name, bytes, it) }
  }

  override fun loadScore(name: String, fileSource: FileSource): ByteArray? {
    return try {
      fileSource.getDir()?.let { loadFile(name, it) }
    } catch (e: Exception) {
      try {
        fileSource.getDir()?.let { loadFile("$name.$SAVE_EXTENSION", it) }
      } catch (e: Exception) {
        null
      }
    }
  }

  override fun deleteScore(name: String, fileSource: FileSource) {
    fileSource.getDir()?.let { deleteFile(name, it) }
  }

  override fun getSoundFonts(): List<File> {
    soundFontDir.listFiles()?.forEach {
      ksLogt("SF $it")
    }
    return soundFontDir.listFiles()?.toList() ?: listOf()
  }

  override fun getSoundFontPath(): String {
    return soundFontDir.absolutePath
  }

  override fun addSoundFont(bytes: ByteArray, name: String) {
    val dest = File(soundFontDir, name)
    FileUtils.writeByteArrayToFile(dest, bytes)
    ksLogt("Wrote $name to $dest")
  }

  override fun deleteSoundFont(name: String) {
    File(soundFontDir, "$name.sf2").delete()
  }

  override fun getDefaultSoundFont(): String {
    return DEFAULT_SOUNDFONT
  }

  override fun resolveString(id: Int): String {
    return context.getString(id)
  }

  @TestOnly
  fun deleteAllFiles() {
    FileSource.values().forEach {
      it.getDir()?.deleteRecursively()
    }
  }

  private fun FileSource.getDir(): File? {
    return when (this) {
      FileSource.SAVE -> saveDir
      FileSource.TEMPLATE -> templateDir
      FileSource.AUTOSAVE -> autoSaveDir
      FileSource.EXTERNAL -> externalStorageDir
      FileSource.SOUNDFONT -> null
      FileSource.THUMBNAIL -> null
    }
  }

  private fun saveFile(name: String, bytes: ByteArray, dir: File) {
    val safeName = name.replace(Regex("[:\\\\/*?|<>]"), "")
    val dest = File(dir, safeName)
    FileUtils.writeByteArrayToFile(dest, bytes)
  }

  private fun loadFile(name: String, dir: File): ByteArray {
    val src = File(dir, name)
    return FileUtils.readFileToByteArray(src)
  }

  private fun deleteFile(name: String, dir: File) {
    val src = File(dir, name)
    FileUtils.deleteQuietly(src)
  }

  private fun loadFromAssets() {
    context.assets.list("soundfont")?.forEach {
      writeAsset("soundfont/$it", soundFontDir)
    }
    writeAsset(DTD, instrumentDataDir)
    loadRawInstrumentData()
  }

  private fun loadFromCacheDir() {
    var oldDir = File(context.cacheDir, "save")
    oldDir.listFiles()?.toList()?.forEach { file ->
      FileUtils.copyFile(file, File(saveDir, file.name))
    }
    oldDir.deleteRecursively()
    oldDir = File(context.cacheDir, "autosave")
    oldDir.listFiles()?.toList()?.forEach { file ->
      FileUtils.copyFile(file, File(autoSaveDir, file.name))
    }
    oldDir.deleteRecursively()
  }

  private fun writeAsset(path: String, destination: File) {
    val iStream = context.assets.open(path)
    val bytes = IOUtils.toByteArray(iStream)
    val destFile = File(destination, FilenameUtils.getBaseName(path))
    FileUtils.writeByteArrayToFile(destFile, bytes)
  }

  private fun loadRawInstrumentData() {
    val iStream = context.assets.open(INSTRUMENT_BASE_DATA)
    val bytes = IOUtils.toByteArray(iStream)
    val current = String(bytes)
    val previous = getBaseInstrumentDescriptions()
    if (previous != current) {
      previousBaseDescriptions = previous
    }
    FileUtils.writeStringToFile(
      File(instrumentDataDir, INSTRUMENT_BASE_DATA),
      current,
      Charset.defaultCharset()
    )
  }
}

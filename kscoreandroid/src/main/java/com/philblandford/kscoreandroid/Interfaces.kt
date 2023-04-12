import com.philblandford.kscore.api.InstrumentGroup
import com.philblandford.kscore.engine.core.area.factory.TextType
import com.philblandford.kscore.engine.types.FileSource
import com.philblandford.kscore.resource.ImageDesc
import com.philblandford.mp3converter.ISampler
import java.io.File
import java.io.InputStream

data class FileInfo(val name:String, val path:String, val fileSource: FileSource, val accessTime:Long)
interface Preferences {
  fun <T> getPreference(key: String, default: T): T
  fun <T> setPreference(key: String, value: T)
  fun getFont(textType: TextType): String
  fun setFont(textType: TextType, font: String)
  fun getDefaultFont(): String
  fun clear()
}

interface SamplerManager {
  fun getSoundFonts(): List<String>
  fun getSampler(soundfont: String, bank: Int): ISampler?
  fun closeSamplers()
  fun reloadSoundFonts()
  fun getSoundfontInstruments(): Map<String, InstrumentGroup>
}

interface TextFontManager {
  fun addTextFont(bytes: ByteArray, name: String)
  fun getTextFont(name: String): InputStream?
  fun getTextFontPath(name: String): String?
  fun getTextFontPath(name:String?, textType: TextType?):String?
  fun getTextFonts(): List<String>
  fun getDefaultTextFont():String
  fun deleteTextFonts()
}

interface ResourceManager : TextFontManager {
  fun getBaseInstrumentDescriptions(): String?
  fun getPreviousBaseInstrumentDescriptions(): String?
  fun getUserInstrumentDescriptions(): String?
  fun saveInstrumentDescriptions(string: String)
  fun clearInstrumentDescriptions()
  fun getSavedFiles(fileSource: FileSource = FileSource.SAVE): List<FileInfo>
  fun saveScore(name: String, bytes: ByteArray, fileSource: FileSource = FileSource.SAVE)
  fun loadScore(name: String, fileSource: FileSource = FileSource.SAVE): ByteArray?
  fun deleteScore(name: String, fileSource: FileSource = FileSource.SAVE)
  fun getSoundFonts(): List<File>
  fun getSoundFontPath(): String
  fun getDefaultSoundFont(): String
  fun addSoundFont(bytes: ByteArray, name: String)
  fun deleteSoundFont(name: String)
  fun getImage(key: String): ImageDesc?
  fun getAllImages(): Iterable<ImageDesc>
  fun resolveString(id: Int): String
  fun getDtdPath():String
}


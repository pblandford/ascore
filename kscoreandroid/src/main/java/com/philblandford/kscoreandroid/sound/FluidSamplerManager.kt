package com.philblandford.kscoreandroid.sound

import ResourceManager
import SamplerManager
import com.philblandford.kscore.api.Instrument
import com.philblandford.kscore.api.InstrumentGroup
import com.philblandford.kscore.engine.types.ClefType
import com.philblandford.kscore.log.ksLoge
import com.philblandford.mp3converter.ISampler
import com.philblandford.mp3converter.engine.sample.FluidSampler
import org.apache.commons.io.FilenameUtils
import java.io.File

class FluidSamplerManager(private val resourceManager: ResourceManager):SamplerManager {

  private val samplers = mutableMapOf<String, FluidSampler>()
  private var soundFontFiles = listOf<File>()
  private var userInstruments = mapOf<String, InstrumentGroup>()

  init {
    try {
      reloadSoundFonts()
    } catch (e:Exception) {
      ksLoge("Could not load soundfonts", e)
    }
  }

  override fun getSoundFonts(): List<String> {
    return soundFontFiles.map { FilenameUtils.getBaseName(it.name) }
  }

  override fun getSampler(soundfont: String, bank: Int): ISampler? {
    val resolved = soundfont.resolve()
    return samplers[soundfont] ?: run {
      createSampler(resolved, bank)?.let { sampler ->
        samplers += soundfont to sampler
        sampler
      }
    }
  }

  override fun closeSamplers() {
    samplers.forEach { it.value.close() }
    samplers.clear()
  }

  private fun getSamplerNum(channel: Int): Int {
    return channel
  }

  private fun createSampler(soundFontPath: String, bank: Int, withDriver:Boolean = true): FluidSampler? {
    return try {
      val sampler = FluidSampler(soundFontPath) // if (withDriver) FluidSampler(soundFontPath) else FluidConvertSampler(soundFontPath)
      sampler.open()
      sampler
    } catch (e: Exception) {
      ksLoge("Could not create sampler for $soundFontPath", e)
      null
    }
  }

  override fun reloadSoundFonts() {
    soundFontFiles = resourceManager.getSoundFonts()

    val presets = soundFontFiles.flatMap { file ->
      createSampler(file.absolutePath, 0, false)?.let { sampler ->
        val p = sampler.getPresets()
        sampler.close()
        p
      } ?: listOf()
    }
    val user = presets.filter { it.soundFont != DEFAULT_SOUNDFONT }
    userInstruments = user.map { preset ->
      preset.name to Instrument(
        preset.name,
        preset.name,
        preset.group,
        preset.program,
        0,
        listOf(ClefType.TREBLE),
        preset.soundFont,
        preset.bank
      )
    }.groupBy { it.second.group }
      .map { it.key to InstrumentGroup(it.key, it.value.map { it.second }) }.toMap()
  }


  override fun getSoundfontInstruments(): Map<String, InstrumentGroup> {
    return userInstruments
  }

  fun addSoundFont(soundFont: ByteArray, name: String) {
    resourceManager.addSoundFont(soundFont, name)
    reloadSoundFonts()
  }

  private fun String.resolve(): String {
    val soundFontKnown = getSoundFonts().any { it == this }
    val name = if (isEmpty() || this == "default" || !soundFontKnown) DEFAULT_SOUNDFONT else this
    return "${resourceManager.getSoundFontPath()}/$name"
  }
}
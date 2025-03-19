package com.philblandford.mp3converter.engine.sample

import android.util.Log
import com.philblandford.mp3converter.engine.SAMPLE_RATE
import com.philblandford.mp3converter.ISampler
import com.philblandford.mp3converter.Ms
import com.philblandford.mp3converter.Preset
import com.philblandford.mp3converter.engine.file.input.*
import org.apache.commons.io.FilenameUtils


open class FluidSampler(protected val soundFontPath: String) : ISampler {

  protected var handle: Long = -1L
  private var haveLibrary = false

  init {
    try {
      System.loadLibrary("fluid-native")
      haveLibrary = true
    } catch (e: Exception) {
      Log.e("FLD", "Could not open fluid")
    }
  }

  private fun <T> tryOp(op: () -> T): T? {
    return if (handle == -1L) {
      null
    } else {
      op()
    }
  }

  override fun open() {
    try {
      if (haveLibrary) {
        handle = openFluid(soundFontPath, true)
        if (handle == -1L) {
          throw Exception("Could not open synth")
        }
      }
    } catch (e: Exception) {
      Log.e("FLD", "Could not open synth")
    }
  }

  override fun close() {
    tryOp {
      closeFluid(handle)
      handle = -1;
    }
  }

  override fun passEvent(midiEvent: MidiEvent) {
    tryOp {
      Log.v("FLD", "Passing event $midiEvent")
      val channel = midiEvent.channel
      when (midiEvent) {
        is ProgramChangeEvent -> {
          programChange(handle, channel, midiEvent.program)
        }

        is NoteOnEvent -> noteOn(handle, channel, midiEvent.midiVal, midiEvent.velocity)
        is NoteOffEvent -> noteOff(handle, channel, midiEvent.midiVal)
        is PedalEvent -> pedal(handle, channel, midiEvent.on)
        else -> {}
      }
    }
  }

  override fun getSample(length: Ms): List<Short> {

    return tryOp {
      if (length < 0) {
        Log.e("FLDS", "Length is $length")
        listOf()
      } else {

        val numShorts = (length * SAMPLE_RATE / 1000) / 1000
        val dataShorts = getSampleData(handle, numShorts).toList()
        dataShorts
      }
    } ?: listOf()
  }

  override fun getPresets(): List<Preset> {
    return tryOp {
      val str = getPresets(handle)
      val lines = str.split("\n")
      lines.mapNotNull { line ->
        val fields = line.split(",")
        fields.getOrNull(0)?.let { name ->
          fields.getOrNull(1)?.toIntOrNull()?.let { program ->
            fields.getOrNull(2)?.let { FilenameUtils.getBaseName(it) }?.let { soundFont ->
              fields.getOrNull(3)?.toIntOrNull()?.let { bank ->
                Preset(name.trim(), soundFont, program, soundFont, bank)
              }
            }
          }
        }
      }
    } ?: listOf()
  }

  override fun numChannels(): Int {
    return 32
  }
}

class FluidConvertSampler(soundFontPath: String) : FluidSampler(soundFontPath) {
  override fun open() {
    handle = openFluid(soundFontPath, false)
    if (handle == -1L) {
      throw Exception("Could not open synth")
    }
  }
}

external fun openFluid(soundFontPath: String, createDriver: Boolean): Long
external fun closeFluid(handle: Long)
external fun getSampleData(handle: Long, length: Long): ShortArray
external fun programChange(handle: Long, channel: Int, midiId: Int)
external fun noteOn(handle: Long, channel: Int, midiVal: Int, velocity: Int)
external fun noteOff(handle: Long, channel: Int, midiVal: Int)
external fun pedal(handle: Long, channel: Int, on: Boolean)
external fun getPresets(handle: Long): String
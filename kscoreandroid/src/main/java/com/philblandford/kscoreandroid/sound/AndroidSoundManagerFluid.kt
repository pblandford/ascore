package com.philblandford.kscoreandroid.sound

import SamplerManager
import com.philblandford.kscore.api.Instrument
import com.philblandford.kscore.api.InstrumentGetter
import com.philblandford.kscore.api.InstrumentGroup
import com.philblandford.kscore.api.SoundManager
import com.philblandford.kscore.log.ksLogd
import com.philblandford.kscore.log.ksLogt
import com.philblandford.mp3converter.ISampler
import com.philblandford.mp3converter.engine.file.input.NoteOffEvent
import com.philblandford.mp3converter.engine.file.input.NoteOnEvent
import com.philblandford.mp3converter.engine.file.input.PedalEvent
import com.philblandford.mp3converter.engine.file.input.ProgramChangeEvent
import java.util.*
import kotlin.concurrent.schedule

internal const val DEFAULT_SOUNDFONT = "chaos"

class AndroidSoundManagerFluid(
  private val instrumentGetter: InstrumentGetter,
  private val samplerManager: SamplerManager
) : SoundManager {

  private val samplers = mutableMapOf<Int, ISampler>()
  override fun soundSingleNote(
    midiVal: Int,
    program: Int,
    velocity: Int,
    length: Int,
    percussion: Boolean,
    soundFont: String,
    bank: Int,
    channel: Int
  ) {
    val actualChannel = if (percussion) 9 else channel
    samplerManager.getSampler(soundFont, bank)?.let { sampler ->
      sampler.passEvent(ProgramChangeEvent(program, actualChannel, soundFont, bank))
      sampler.passEvent(NoteOnEvent(midiVal, velocity, actualChannel))
      Timer().schedule(length.toLong()) {
        sampler.passEvent(NoteOffEvent(midiVal, 0, actualChannel))
      }
    }
  }

  override fun soundSingleNoteNoStop(
    midiVal: Int,
    program: Int,
    velocity: Int,
    percussion: Boolean,
    soundFont: String,
    bank: Int,
    channel: Int
  ) {
    samplerManager.getSampler(soundFont, bank)?.let { sampler ->
      sampler.passEvent(ProgramChangeEvent(program, channel, soundFont, bank))
      sampler.passEvent(NoteOnEvent(midiVal, velocity, channel))
    }
  }

  override fun noteOn(midiVal: Int, velocity: Int, channel: Int) {
    samplers[channel]?.passEvent(NoteOnEvent(midiVal, velocity, channel))
  }

  override fun noteOff(midiVal: Int, channel: Int) {
    samplers[channel]?.passEvent(NoteOffEvent(midiVal, 0, channel))
  }

  override fun programChange(program: Int, channel: Int, soundFont: String, bank: Int) {
    samplerManager.getSampler(soundFont, bank)?.let { sampler ->
      samplers += channel to sampler
      samplers[channel]?.passEvent(ProgramChangeEvent(program, channel, soundFont, bank))
    }
  }

  override fun pedalEvent(on: Boolean, channel: Int) {
    ksLogt("pedal event $on $channel")
    samplers[channel]?.passEvent(PedalEvent(channel, on))
  }

  override fun stopAllNotes(soundFont: String?) {
    soundFont?.let { samplerManager.getSampler(it, 0)?.let { sampler ->
      stopAllNotes(sampler)
    } } ?: run {
      samplers.toList().groupBy{it.second}. forEach { (sampler, _) ->
        stopAllNotes(sampler)
      }
    }
  }

  private fun stopAllNotes(sampler: ISampler) {
    ksLogt("Stopping notes for ${sampler}")
    (0..sampler.numChannels()).forEach { channel ->
      (0..100).forEach { midiVal ->
        sampler.passEvent(NoteOffEvent(midiVal, 0, channel))
      }
      sampler.passEvent(PedalEvent(channel, false))
    }
  }

  override fun reset() {
    ksLogd("Resetting synth")
  }

  override fun close() {
    samplerManager.closeSamplers()
    samplers.clear()
  }

  override fun getInstrumentGroup(name: String): InstrumentGroup? {
    return instrumentGetter.getInstrumentGroups().find { it.name == name }
  }

  override fun getInstrumentGroups(): List<InstrumentGroup> {
    return instrumentGetter.getInstrumentGroups().filter { it.instruments.isNotEmpty() }
  }

  override fun getInstrument(name: String): Instrument? {
    return instrumentGetter.getInstrumentGroups().flatMap { it.instruments }
      .find { it.name == name }
  }

  override fun getInstrument(programId: Int): Instrument? {
    return instrumentGetter.getInstrumentGroups().flatMap { it.instruments }
      .find { it.program == programId }
  }

  override fun createGroup(name: String, instruments: List<Instrument>) {
    instrumentGetter.createGroup(name, instruments)
  }

  override fun refresh() {
    samplerManager.reloadSoundFonts()
    instrumentGetter.refresh()
  }

  override fun clearUser() {
    instrumentGetter.clearUser()
  }

  override fun assignInstrument(instrumentName: String, group: String) {
    instrumentGetter.assignInstrument(instrumentName, group)
  }
}